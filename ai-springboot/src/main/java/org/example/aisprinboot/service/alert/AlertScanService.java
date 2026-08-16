package org.example.aisprinboot.service.alert;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.entity.AlertRecord;
import org.example.aisprinboot.entity.EmotionDiary;
import org.example.aisprinboot.enumClass.AlertLevel;
import org.example.aisprinboot.mapper.AlertRecordMapper;
import org.example.aisprinboot.mapper.EmotionDiaryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 危机预警扫描编排服务
 * <p>
 * 职责：查询日记 → 规则检测 → AI 分析 → 写库 → 通知
 * <p>
 * 设计要点：
 * 1. 双轨入口：
 *    - executeScan(date)：批量轨，定时任务和手动触发都调它
 *    - handleHighRiskDiary(diary, result)：实时轨，日记创建时调用
 * 2. 事务边界精确：
 *    - AI 调用（可能 10s）绝不在事务里
 *    - 只在 saveAlertWithNotification 上加 @Transactional
 * 3. 幂等：alert_record.uk_user_diary 唯一约束 + try-catch 兜底
 * 4. 并发防护：AtomicBoolean 标志位，扫描进行中跳过新触发
 *
 * @author PANJU
 */
@Slf4j
@Service
public class AlertScanService {

    @Autowired
    private EmotionDiaryMapper diaryMapper;

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private AlertRuleEngine ruleEngine;

    @Autowired
    private AlertAnalysisService analysisService;

    @Autowired
    private AlertNotificationService notificationService;

    @Autowired
    private ArticleRecommendationService recommendationService;

    /**
     * 扫描进行中标志位（防并发）
     */
    private final java.util.concurrent.atomic.AtomicBoolean scanning =
            new java.util.concurrent.atomic.AtomicBoolean(false);

    // ============================================================
    // 入口1：批量轨（定时任务 + 手动触发）
    // ============================================================

    /**
     * 扫描指定日期的所有日记，生成预警
     *
     * @param date 目标日期
     */
    public void executeScan(LocalDate date) {
        if (!scanning.compareAndSet(false, true)) {
            log.warn("扫描进行中，跳过本次触发 date={}", date);
            return;
        }
        try {
            log.info("开始批量扫描 date={}", date);

            // 1. 查询当天所有日记（无事务）
            LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EmotionDiary::getDiaryDate, date);
            List<EmotionDiary> diaries = diaryMapper.selectList(wrapper);
            log.info("扫描到 {} 篇日记 date={}", diaries.size(), date);

            int alertCount = 0;
            for (EmotionDiary diary : diaries) {
                try {
                    alertCount += processDiary(diary);
                } catch (Exception e) {
                    // 单篇失败不影响后续处理
                    log.error("处理日记失败 diaryId={}", diary.getId(), e);
                }
            }
            log.info("批量扫描完成 date={}, 共生成预警 {} 条", date, alertCount);
        } finally {
            scanning.set(false);
        }
    }

    // ============================================================
    // 入口2：实时轨（日记创建时）
    // ============================================================

    /**
     * 处理高危日记（实时轨入口）
     * 由 EmotionDiaryService.create() 在日记入库后调用
     * <p>
     * 注意：本方法在调用方线程同步执行 AI 分析和写库，
     * 失败时由调用方 try-catch 兜住，不影响日记创建本身的成功返回。
     *
     * @param diary  已入库的日记
     * @param result 规则引擎评估结果（已经判定为需要预警）
     */
    public void handleHighRiskDiary(EmotionDiary diary, AlertRuleResult result) {
        // 实时轨不抢 scanning 标志位（避免高危日记被忽略），但靠唯一约束 uk_user_diary 防重
        log.info("实时预警触发 diaryId={}, level={}", diary.getId(), result.getLevel());
        try {
            // AI 分析 + 写库（无事务包裹 AI 调用）
            String analysis = analysisService.analyze(diary);
            List<String> articleIds = recommendationService.recommend(
                    diary.getDominantEmotion(), diary.getEmotionTriggers());
            saveAlertWithNotification(diary, result, analysis, articleIds);
        } catch (Exception e) {
            log.error("实时预警处理失败 diaryId={}", diary.getId(), e);
        }
    }

    // ============================================================
    // 内部处理逻辑（批量轨复用）
    // ============================================================

    /**
     * 处理单篇日记：规则检测 → AI 分析 → 写库 → 通知
     *
     * @param diary 情绪日记
     * @return 1 表示生成预警，0 表示无需预警
     */
    private int processDiary(EmotionDiary diary) {
        // 1. 规则检测（纯内存，毫秒级）
        AlertRuleResult result = ruleEngine.evaluate(diary);
        if (!result.shouldAlert()) {
            return 0;
        }

        // 2. 幂等检查：该日记是否已生成过预警
        if (hasExistingAlert(diary.getUserId(), diary.getId())) {
            log.debug("日记已有预警记录，跳过 diaryId={}", diary.getId());
            return 0;
        }

        // 3. AI 分析（耗时操作，无事务）
        String analysis = analysisService.analyze(diary);

        // 4. 推荐文章
        List<String> articleIds = recommendationService.recommend(
                diary.getDominantEmotion(), diary.getEmotionTriggers());

        // 5. 写库（事务边界精确，仅包含数据库操作）
        saveAlertWithNotification(diary, result, analysis, articleIds);
        return 1;
    }

    /**
     * 写预警记录 + 创建通知（事务边界）
     * <p>
     * 事务范围：仅包含数据库写操作，不包含 AI 调用
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveAlertWithNotification(EmotionDiary diary, AlertRuleResult result,
                                          String analysis, List<String> articleIds) {
        // 构建预警记录
        AlertRecord record = new AlertRecord();
        record.setUserId(diary.getUserId());
        record.setDiaryId(diary.getId());
        record.setAlertLevel(result.getLevel().getCode());
        record.setTriggerReason(JSONUtil.toJsonStr(result.getTriggeredRules()));
        record.setAiAnalysis(analysis);
        record.setRecommendedArticles(JSONUtil.toJsonStr(articleIds));
        record.setStatus(0); // 未处理
        record.setCreatedAt(LocalDateTime.now());

        // 幂等兜底：唯一约束 uk_user_diary 冲突时 try-catch
        try {
            alertRecordMapper.insert(record);
            log.info("预警记录已生成 diaryId={}, level={}", diary.getId(), result.getLevel());
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.info("预警记录已存在（唯一约束冲突），跳过 diaryId={}", diary.getId());
            return;
        }

        // 创建通知（站内 + 异步邮件）
        notificationService.createNotifications(record.getId(), record);
    }

    /**
     * 检查某篇日记是否已有预警记录
     */
    private boolean hasExistingAlert(Long userId, Long diaryId) {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRecord::getUserId, userId)
                .eq(AlertRecord::getDiaryId, diaryId);
        return alertRecordMapper.selectCount(wrapper) > 0;
    }
}
