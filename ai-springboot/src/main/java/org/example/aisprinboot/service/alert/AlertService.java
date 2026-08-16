package org.example.aisprinboot.service.alert;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.entity.AlertRecord;
import org.example.aisprinboot.entity.EmotionDiary;
import org.example.aisprinboot.entity.KnowledgeArticle;
import org.example.aisprinboot.entity.User;
import org.example.aisprinboot.enumClass.AlertStatus;
import org.example.aisprinboot.exception.BusionessException;
import org.example.aisprinboot.mapper.AlertRecordMapper;
import org.example.aisprinboot.mapper.EmotionDiaryMapper;
import org.example.aisprinboot.mapper.KnowledgeArticleMapper;
import org.example.aisprinboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 危机预警记录 CRUD 服务（给 AlertController 用）
 * <p>
 * 与 AlertScanService（扫描逻辑）职责分离：
 * - AlertScanService：生成预警（写）
 * - AlertService：查询、详情、处理（读+改）
 *
 * @author PANJU
 */
@Slf4j
@Service
public class AlertService {

    @Autowired
    private AlertRecordMapper alertRecordMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmotionDiaryMapper diaryMapper;

    @Autowired
    private KnowledgeArticleMapper articleMapper;

    /**
     * 分页查询预警列表
     *
     * @param page    分页对象
     * @param level   预警级别（可选）
     * @param status  处理状态（可选）
     * @return 分页结果
     */
    public Page<AlertRecord> page(Page<AlertRecord> page, Integer level, Integer status) {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        if (level != null) {
            wrapper.eq(AlertRecord::getAlertLevel, level);
        }
        if (status != null) {
            wrapper.eq(AlertRecord::getStatus, status);
        }
        wrapper.orderByDesc(AlertRecord::getCreatedAt);

        Page<AlertRecord> result = alertRecordMapper.selectPage(page, wrapper);

        // 填充用户昵称和日记预览
        fillUserInfo(result.getRecords());
        return result;
    }

    /**
     * 获取预警详情（含 AI 分析、推荐文章、关联日记内容）
     *
     * @param id 预警ID
     * @return 预警记录（含扩展信息）
     */
    public AlertRecord getDetail(Long id) {
        AlertRecord record = alertRecordMapper.selectById(id);
        if (record == null) {
            throw new BusionessException("预警记录不存在");
        }

        // 填充用户昵称
        User user = userMapper.selectById(record.getUserId());
        if (user != null) {
            record.setUserNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
        }

        // 填充日记内容预览
        EmotionDiary diary = diaryMapper.selectById(record.getDiaryId());
        if (diary != null && diary.getDiaryContent() != null) {
            String preview = diary.getDiaryContent();
            if (preview.length() > 200) {
                preview = preview.substring(0, 200) + "...";
            }
            record.setDiaryContentPreview(preview);
        }

        return record;
    }

    /**
     * 处理预警（标记为已处理 / 已忽略）
     *
     * @param id        预警ID
     * @param status    目标状态：1已处理 2已忽略
     * @param handlerId 处理人ID（当前登录管理员）
     */
    public void handle(Long id, Integer status, Long handlerId) {
        AlertRecord record = alertRecordMapper.selectById(id);
        if (record == null) {
            throw new BusionessException("预警记录不存在");
        }
        if (!AlertStatus.isValidCode(status)) {
            throw new BusionessException("无效的处理状态");
        }

        record.setStatus(status);
        record.setHandledBy(handlerId);
        record.setHandledAt(LocalDateTime.now());
        alertRecordMapper.updateById(record);
        log.info("预警已处理 alertId={}, status={}, handlerId={}", id, status, handlerId);
    }

    /**
     * 获取统计数据（给仪表盘用）
     */
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();

        // 总数
        Long total = alertRecordMapper.selectCount(null);
        stats.put("total", total);

        // 未处理数
        LambdaQueryWrapper<AlertRecord> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.eq(AlertRecord::getStatus, AlertStatus.PENDING.getCode());
        stats.put("pending", alertRecordMapper.selectCount(pendingWrapper));

        // 高危数
        LambdaQueryWrapper<AlertRecord> highWrapper = new LambdaQueryWrapper<>();
        highWrapper.eq(AlertRecord::getAlertLevel, 3); // HIGH
        stats.put("highRisk", alertRecordMapper.selectCount(highWrapper));

        // 今日新增
        LambdaQueryWrapper<AlertRecord> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.ge(AlertRecord::getCreatedAt, LocalDateTime.now().toLocalDate().atStartOfDay());
        stats.put("todayNew", alertRecordMapper.selectCount(todayWrapper));

        return stats;
    }

    /**
     * 获取用户自己的关怀推荐文章（用于关怀页，不显示敏感信息）
     * <p>
     * 设计要点：
     * 1. 用户只能看到推荐文章，看不到"被预警"的字眼；
     * 2. 用 selectBatchIds 批量查询文章详情，避免前端 N+1 调用；
     * 3. 仅返回已发布文章，并清空 content 字段（正文过长，关怀页只需摘要）。
     *
     * @param userId 用户ID
     * @return 推荐文章列表（最近 30 天内的预警对应的推荐文章）
     */
    public List<KnowledgeArticle> getMyRecommendedArticles(Long userId) {
        LambdaQueryWrapper<AlertRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AlertRecord::getUserId, userId)
                .isNotNull(AlertRecord::getRecommendedArticles)
                .ge(AlertRecord::getCreatedAt, LocalDateTime.now().minusDays(30).toLocalDate().atStartOfDay())
                .orderByDesc(AlertRecord::getCreatedAt);

        List<AlertRecord> records = alertRecordMapper.selectList(wrapper);

        // 收集所有推荐文章 ID 并去重（保留插入顺序）
        Set<String> articleIds = new LinkedHashSet<>();
        for (AlertRecord record : records) {
            if (record.getRecommendedArticles() != null) {
                try {
                    List<String> ids = JSONUtil.toList(record.getRecommendedArticles(), String.class);
                    articleIds.addAll(ids);
                } catch (Exception e) {
                    log.warn("解析推荐文章 JSON 失败 alertId={}", record.getId());
                }
            }
        }

        // 没有推荐文章，直接返回空列表
        if (articleIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询文章详情，仅保留已发布的（status=1）
        List<KnowledgeArticle> articles = articleMapper.selectBatchIds(articleIds);
        List<KnowledgeArticle> result = new ArrayList<>();
        for (KnowledgeArticle article : articles) {
            if (article.getStatus() != null && article.getStatus() == 1) {
                // 清空正文，关怀页只展示摘要，避免传输大字段
                article.setContent(null);
                result.add(article);
            }
        }
        return result;
    }

    /**
     * 批量填充用户昵称
     */
    private void fillUserInfo(List<AlertRecord> records) {
        if (records.isEmpty()) {
            return;
        }
        // 收集所有 userId
        Set<Long> userIds = records.stream()
                .map(AlertRecord::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (userIds.isEmpty()) {
            return;
        }

        // 批量查询用户
        List<User> users = userMapper.selectBatchIds(userIds);
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 填充昵称
        for (AlertRecord record : records) {
            User user = userMap.get(record.getUserId());
            if (user != null) {
                record.setUserNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
            }
        }
    }
}
