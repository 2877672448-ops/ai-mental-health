package org.example.aisprinboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aisprinboot.common.Result;
import org.example.aisprinboot.entity.EmotionDiary;
import org.example.aisprinboot.entity.User;
import org.example.aisprinboot.service.EmotionDiaryService;
import org.example.aisprinboot.mapper.UserMapper;
import org.example.aisprinboot.util.JwtTokenUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 情绪日记控制器
 */
@RestController
@RequestMapping("/api/emotion-diary")
public class EmotionDiaryController {

    @Autowired
    private EmotionDiaryService diaryService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 创建情绪日记（普通用户）
     */
    @PostMapping
    public Result<?> createDiary(@RequestBody EmotionDiary diary) {
        Long userId = getCurrentUserId();
        return Result.ok(diaryService.create(diary, userId));
    }

    /**
     * 获取当前用户的日记列表
     */
    @GetMapping("/my/page")
    public Result<?> getMyDiaryPage(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = getCurrentUserId();
        Page<EmotionDiary> page = new Page<>(currentPage, size);
        return Result.ok(diaryService.userPage(page, userId));
    }

    /**
     * 管理员分页查询
     */
    @GetMapping("/admin/page")
    public Result<?> adminPage(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String dominantEmotion) {

        // 校验管理员权限
        checkAdmin();

        Page<EmotionDiary> page = new Page<>(currentPage, size);
        Map<String, Object> params = new HashMap<>();
        if (userId != null) params.put("userId", userId);
        if (keyword != null) params.put("keyword", keyword);
        if (dominantEmotion != null) params.put("dominantEmotion", dominantEmotion);

        return Result.ok(diaryService.adminPage(page, params));
    }

    /**
     * 管理员删除
     */
    @DeleteMapping("/admin/{id}")
    public Result<?> adminDelete(@PathVariable Long id) {
        // 校验管理员权限
        checkAdmin();
        diaryService.adminDelete(id);
        return Result.ok();
    }

    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        return jwt.getClaim("userId").asLong();
    }

    /**
     * 校验管理员权限
     */
    private void checkAdmin() {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null || user.getUserType() == null || user.getUserType() != 2) {
            throw new org.example.aisprinboot.exception.BusionessException("权限不足");
        }
    }
}
