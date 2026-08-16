package org.example.aisprinboot.service.alert;

import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.entity.EmotionDiary;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 危机预警 AI 深度分析服务
 * <p>
 * 使用纯净的 analysis-ai ChatClient（无人设、无记忆 Advisor），
 * 对情绪日记做结构化风险评估，输出危机等级、风险因素、干预建议。
 * <p>
 * 设计要点：
 * 1. 同步调用 chatClient.prompt().call().content()，非流式
 * 2. 一次调用可能耗时 5-10 秒，调用方需自行控制并发（参考 AlertScanService）
 * 3. 异常向上抛，由 AlertScanService 逐条 try-catch
 *
 * @author PANJU
 */
@Slf4j
@Service
public class AlertAnalysisService {

    /**
     * 危机分析专用 ChatClient（不携带对话记忆和心理疏导师人设）
     */
    @Autowired
    @Qualifier("analysis-ai")
    private ChatClient analysisClient;

    /**
     * 分析情绪日记的危机程度
     *
     * @param diary 情绪日记
     * @return AI 分析结果（文本，包含危机等级、风险因素、干预建议）
     */
    public String analyze(EmotionDiary diary) {
        String prompt = """
                你是心理危机评估专家。请基于以下情绪日记信息进行结构化风险评估：

                【情绪评分】%d/10（分数越低越危险）
                【主要情绪】%s
                【压力水平】%d/5
                【睡眠质量】%d/5
                【触发因素】%s
                【日记内容】
                %s

                请按以下格式输出（简洁、专业）：
                1. 危机等级：[低危/中危/高危]
                2. 主要风险因素：（列出 1-3 条）
                3. 建议干预措施：（列出 1-3 条具体可执行的建议）
                4. 紧急程度：[可观察/需关注/需立即介入]
                """.formatted(
                diary.getMoodScore() != null ? diary.getMoodScore() : 0,
                diary.getDominantEmotion() != null ? diary.getDominantEmotion() : "未填写",
                diary.getStressLevel() != null ? diary.getStressLevel() : 0,
                diary.getSleepQuality() != null ? diary.getSleepQuality() : 0,
                diary.getEmotionTriggers() != null ? diary.getEmotionTriggers() : "未填写",
                diary.getDiaryContent() != null ? diary.getDiaryContent() : "（无内容）"
        );

        log.debug("开始分析日记 diaryId={}", diary.getId());
        String result = analysisClient.prompt(prompt).call().content();
        log.debug("分析完成 diaryId={}", diary.getId());
        return result;
    }
}
