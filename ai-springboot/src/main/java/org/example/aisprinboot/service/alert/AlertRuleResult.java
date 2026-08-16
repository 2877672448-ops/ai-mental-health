package org.example.aisprinboot.service.alert;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.aisprinboot.enumClass.AlertLevel;

import java.util.List;

/**
 * 规则引擎评估结果值对象
 * 纯数据载体，无副作用，便于单测
 *
 * @author PANJU
 */
@Data
@AllArgsConstructor
public class AlertRuleResult {

    /**
     * 命中的规则列表（如 "keyword:自杀"、"mood_score_low"、"stress_high"）
     */
    private List<String> triggeredRules;

    /**
     * 预警级别（null 表示无需预警）
     */
    private AlertLevel level;

    /**
     * 综合风险评分
     */
    private int score;

    /**
     * 是否需要预警
     *
     * @return true 表示命中规则需要生成预警
     */
    public boolean shouldAlert() {
        return level != null;
    }
}
