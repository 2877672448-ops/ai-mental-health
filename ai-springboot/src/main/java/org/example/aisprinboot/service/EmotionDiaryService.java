package org.example.aisprinboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.entity.EmotionDiary;
import org.example.aisprinboot.entity.User;
import org.example.aisprinboot.enumClass.AlertLevel;
import org.example.aisprinboot.exception.BusionessException;
import org.example.aisprinboot.mapper.EmotionDiaryMapper;
import org.example.aisprinboot.mapper.UserMapper;
import org.example.aisprinboot.service.alert.AlertRuleEngine;
import org.example.aisprinboot.service.alert.AlertRuleResult;
import org.example.aisprinboot.service.alert.AlertScanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 情绪日记服务
 */
@Slf4j
@Service
public class EmotionDiaryService {

    @Autowired
    private EmotionDiaryMapper diaryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AlertRuleEngine alertRuleEngine;

    @Autowired
    private AlertScanService alertScanService;

    /**
     * 创建情绪日记
     */
    public EmotionDiary create(EmotionDiary diary, Long userId) {
        // 检查当天是否已提交
        LocalDate today = diary.getDiaryDate() != null ? diary.getDiaryDate() : LocalDate.now();
        LambdaQueryWrapper<EmotionDiary> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(EmotionDiary::getUserId, userId)
                .eq(EmotionDiary::getDiaryDate, today);
        Long count = diaryMapper.selectCount(checkWrapper);
        if (count > 0) {
            throw new BusionessException("您今天已经提交过情绪日记了");
        }

        diary.setUserId(userId);
        if (diary.getDiaryDate() == null) {
            diary.setDiaryDate(today);
        }
        diary.setCreatedAt(LocalDateTime.now());
        diary.setUpdatedAt(LocalDateTime.now());

        diaryMapper.insert(diary);

        // 实时轨：日记入库后立即做规则检测
        // 设计要点：try-catch 包裹，预警检测失败不影响日记创建的成功返回
        try {
            AlertRuleResult result = alertRuleEngine.evaluate(diary);
            if (result.shouldAlert() && result.getLevel() == AlertLevel.HIGH) {
                // 仅高危立即触发 AI 分析 + 通知（耗时操作）
                // 中危/低危留给批量任务处理，避免日记接口被拖慢
                log.info("检测到高危情绪日记 diaryId={}, score={}", diary.getId(), result.getScore());
                alertScanService.handleHighRiskDiary(diary, result);
            }
        } catch (Exception e) {
            log.error("实时预警检测失败 diaryId={}, 日记创建不受影响", diary.getId(), e);
        }

        return diary;
    }

    /**
     * 管理员分页查询
     */
    public Page<EmotionDiary> adminPage(Page<EmotionDiary> page, Map<String, Object> params) {
        LambdaQueryWrapper<EmotionDiary> queryWrapper = new LambdaQueryWrapper<>();

        // 用户ID过滤
        if (params.containsKey("userId")) {
            queryWrapper.eq(EmotionDiary::getUserId, params.get("userId"));
        }

        // 情绪标签过滤
        if (params.containsKey("dominantEmotion")) {
            queryWrapper.eq(EmotionDiary::getDominantEmotion, params.get("dominantEmotion"));
        }

        // 关键字搜索（内容或触发因素）
        if (params.containsKey("keyword")) {
            String keyword = params.get("keyword").toString();
            queryWrapper.and(w -> w
                    .like(EmotionDiary::getDiaryContent, keyword)
                    .or()
                    .like(EmotionDiary::getEmotionTriggers, keyword)
            );
        }

        queryWrapper.orderByDesc(EmotionDiary::getDiaryDate);

        Page<EmotionDiary> result = diaryMapper.selectPage(page, queryWrapper);

        // 填充用户昵称
        for (EmotionDiary diary : result.getRecords()) {
            if (diary.getUserId() != null) {
                User user = userMapper.selectById(diary.getUserId());
                if (user != null) {
                    diary.setNickname(user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
            }
        }

        return result;
    }

    /**
     * 获取用户自己的日记分页
     */
    public Page<EmotionDiary> userPage(Page<EmotionDiary> page, Long userId) {
        LambdaQueryWrapper<EmotionDiary> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(EmotionDiary::getUserId, userId)
                .orderByDesc(EmotionDiary::getDiaryDate);
        return diaryMapper.selectPage(page, queryWrapper);
    }

    /**
     * 管理员删除
     */
    public void adminDelete(Long id) {
        EmotionDiary diary = diaryMapper.selectById(id);
        if (diary == null) {
            throw new BusionessException("日记不存在");
        }
        diaryMapper.deleteById(id);
    }
}
