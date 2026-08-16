package org.example.aisprinboot.service.alert;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.entity.KnowledgeArticle;
import org.example.aisprinboot.mapper.KnowledgeArticleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 文章推荐服务
 * <p>
 * 根据情绪日记的 dominant_emotion 和触发因素，映射到知识库分类，
 * 推荐相关科普文章给用户（关怀推送，不打"预警"标签）。
 *
 * @author PANJU
 */
@Slf4j
@Service
public class ArticleRecommendationService {

    @Autowired
    private KnowledgeArticleMapper articleMapper;

    /**
     * 根据情绪日记信息推荐文章
     *
     * @param dominantEmotion 主要情绪（如"焦虑"、"抑郁"、"压力"等）
     * @param emotionTriggers 触发因素（如"工作"、"学习"、"人际"等）
     * @return 推荐文章ID列表（UUID 字符串，最多 3 篇）
     */
    public List<String> recommend(String dominantEmotion, String emotionTriggers) {
        // 1. 情绪到分类 ID 的映射（参考 init_data.sql 中的分类编码）
        Long categoryId = mapToCategoryId(dominantEmotion, emotionTriggers);
        if (categoryId == null) {
            log.debug("未匹配到推荐分类，emotion={}, triggers={}", dominantEmotion, emotionTriggers);
            return Collections.emptyList();
        }

        // 2. 查询该分类下已发布的文章，按阅读量倒序取前 3 篇
        LambdaQueryWrapper<KnowledgeArticle> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeArticle::getCategoryId, categoryId)
                .eq(KnowledgeArticle::getStatus, 1)
                .orderByDesc(KnowledgeArticle::getReadCount)
                .last("LIMIT 3");

        List<KnowledgeArticle> articles = articleMapper.selectList(wrapper);
        return articles.stream().map(KnowledgeArticle::getId).toList();
    }

    /**
     * 情绪/触发因素 → 分类ID 映射
     * 分类ID 参考 init_data.sql：
     * 1情绪管理、2压力调节、3人际关系、4自我成长、5睡眠健康
     * 6焦虑与恐慌、7抑郁与低落、8职场压力、9学业压力
     */
    private Long mapToCategoryId(String emotion, String triggers) {
        String text = ((emotion != null ? emotion : "") + " " + (triggers != null ? triggers : "")).toLowerCase();

        // 自杀/自残相关 → 抑郁与低落（最相关）
        if (containsAny(text, "自杀", "想死", "自残", "轻生", "不想活")) {
            return 7L;
        }
        // 抑郁/低落
        if (containsAny(text, "抑郁", "低落", "悲伤", "绝望", "空虚", "抑郁与低落")) {
            return 7L;
        }
        // 焦虑/恐慌
        if (containsAny(text, "焦虑", "恐慌", "紧张", "不安", "害怕", "恐惧")) {
            return 6L;
        }
        // 职场压力
        if (containsAny(text, "工作", "职场", "加班", "上司", "同事", "职业")) {
            return 8L;
        }
        // 学业压力
        if (containsAny(text, "学习", "考试", "学校", "同学", "成绩", "老师")) {
            return 9L;
        }
        // 人际关系
        if (containsAny(text, "人际", "朋友", "家人", "恋爱", "分手", "孤独")) {
            return 3L;
        }
        // 睡眠
        if (containsAny(text, "失眠", "睡眠", "睡不着", "多梦", "疲倦")) {
            return 5L;
        }
        // 压力（通用）
        if (containsAny(text, "压力", "stress", "崩溃", "喘不过气")) {
            return 2L;
        }
        // 默认：自我成长
        return 4L;
    }

    /**
     * 判断 text 是否包含 keywords 中任意一个
     */
    private boolean containsAny(String text, String... keywords) {
        return Arrays.stream(keywords).anyMatch(text::contains);
    }
}
