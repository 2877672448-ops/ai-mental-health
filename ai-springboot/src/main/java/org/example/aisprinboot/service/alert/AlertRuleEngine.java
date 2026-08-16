package org.example.aisprinboot.service.alert;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.entity.AlertKeyword;
import org.example.aisprinboot.entity.EmotionDiary;
import org.example.aisprinboot.enumClass.AlertLevel;
import org.example.aisprinboot.mapper.AlertKeywordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 危机预警规则引擎（纯函数式，无副作用）
 * <p>
 * 设计要点：
 * 1. 入参 EmotionDiary，出参 AlertRuleResult，不碰数据库、不调 AI
 * 2. 关键词从 alert_keyword 表加载到内存缓存，避免每次扫描都查库
 * 3. 综合评分：关键词 risk_weight 累加 + mood_score 低分 + stress_level 高
 * 4. 可被实时轨（日记创建）和批量轨（定时任务）复用
 *
 * @author PANJU
 */
@Slf4j
@Component
public class AlertRuleEngine {

    @Autowired
    private AlertKeywordMapper keywordMapper;

    /**
     * 关键词内存缓存：CopyOnWriteArrayList 保证遍历时不阻塞刷新
     * 系统启动时一次性加载，后续可通过 reloadKeywords() 刷新
     */
    private final List<AlertKeyword> keywordCache = new CopyOnWriteArrayList<>();

    /**
     * 启动时加载所有启用的关键词到内存
     */
    @PostConstruct
    public void loadKeywords() {
        reloadKeywords();
    }

    /**
     * 重新加载关键词缓存（管理员后台修改关键词后可调用）
     */
    public void reloadKeywords() {
        List<AlertKeyword> keywords = keywordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AlertKeyword>()
                        .eq(AlertKeyword::getStatus, 1));
        keywordCache.clear();
        keywordCache.addAll(keywords);
        log.info("危机预警关键词已加载，共 {} 个", keywordCache.size());
    }

    /**
     * 评估情绪日记的危机风险
     * <p>
     * 评分规则：
     * - 关键词命中：累加 risk_weight
     * - mood_score ≤ 3：+5 分
     * - stress_level ≥ 4：+3 分
     * <p>
     * 级别判定：
     * - score ≥ 8：HIGH（高危）
     * - score ≥ 4：MEDIUM（中危）
     * - score > 0：LOW（低危）
     * - score = 0：null（无需预警）
     *
     * @param diary 情绪日记
     * @return 评估结果（level 为 null 表示无需预警）
     */
    public AlertRuleResult evaluate(EmotionDiary diary) {
        if (diary == null) {
            return new AlertRuleResult(new ArrayList<>(), null, 0);
        }

        List<String> triggered = new ArrayList<>();
        int score = 0;

        // 1. 关键词扫描
        String content = diary.getDiaryContent();
        if (content != null && !content.isEmpty()) {
            for (AlertKeyword kw : keywordCache) {
                if (content.contains(kw.getKeyword())) {
                    triggered.add("keyword:" + kw.getKeyword());
                    score += kw.getRiskWeight();
                }
            }
        }

        // 2. mood_score 判断（1-10，越低越危险）
        if (diary.getMoodScore() != null && diary.getMoodScore() <= 3) {
            triggered.add("mood_score_low:" + diary.getMoodScore());
            score += 5;
        } else if (diary.getMoodScore() != null && diary.getMoodScore() <= 5) {
            triggered.add("mood_score_medium:" + diary.getMoodScore());
            score += 2;
        }

        // 3. stress_level 判断（1-5，越高越危险）
        if (diary.getStressLevel() != null && diary.getStressLevel() >= 4) {
            triggered.add("stress_high:" + diary.getStressLevel());
            score += 3;
        }

        // 4. 综合算分定级
        AlertLevel level = score >= 8 ? AlertLevel.HIGH
                : score >= 4 ? AlertLevel.MEDIUM
                : score > 0 ? AlertLevel.LOW : null;

        return new AlertRuleResult(triggered, level, score);
    }
}
