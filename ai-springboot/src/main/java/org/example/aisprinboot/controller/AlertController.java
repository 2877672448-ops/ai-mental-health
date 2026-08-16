package org.example.aisprinboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.example.aisprinboot.common.Result;
import org.example.aisprinboot.entity.AlertRecord;
import org.example.aisprinboot.entity.KnowledgeArticle;
import org.example.aisprinboot.entity.User;
import org.example.aisprinboot.exception.BusionessException;
import org.example.aisprinboot.mapper.UserMapper;
import org.example.aisprinboot.service.alert.AlertService;
import org.example.aisprinboot.service.alert.AlertScanService;
import org.example.aisprinboot.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 危机预警接口
 * <p>
 * 管理员接口：分页查询、详情、处理、手动触发扫描、统计
 * 用户接口：查看自己的关怀推荐文章（不显示"被预警"字眼）
 *
 * @author PANJU
 */
@RestController
@RequestMapping("/api/alert")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private AlertScanService alertScanService;

    @Autowired
    private UserMapper userMapper;

    // ============================================================
    // 管理员接口
    // ============================================================

    /**
     * 分页查询预警列表
     */
    @GetMapping("/page")
    public Result<?> page(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) Integer status) {
        checkAdmin();
        Page<AlertRecord> page = new Page<>(currentPage, size);
        return Result.ok(alertService.page(page, level, status));
    }

    /**
     * 预警详情
     */
    @GetMapping("/{id}")
    public Result<?> detail(@PathVariable Long id) {
        checkAdmin();
        return Result.ok(alertService.getDetail(id));
    }

    /**
     * 处理预警（标记为已处理 / 已忽略）
     *
     * @param id     预警ID
     * @param status 目标状态：1已处理 2已忽略
     */
    @PutMapping("/{id}/handle")
    public Result<?> handle(@PathVariable Long id, @RequestParam Integer status) {
        checkAdmin();
        Long handlerId = getCurrentUserId();
        alertService.handle(id, status, handlerId);
        return Result.ok();
    }

    /**
     * 手动触发扫描（演示用，避免等凌晨 2 点）
     *
     * @param date 目标日期（格式 YYYY-MM-DD，不传默认昨天）
     */
    @PostMapping("/scan/trigger")
    public Result<?> triggerScan(@RequestParam(required = false) String date) {
        checkAdmin();
        LocalDate targetDate = (date != null)
                ? LocalDate.parse(date)
                : LocalDate.now().minusDays(1);
        alertScanService.executeScan(targetDate);
        return Result.ok("扫描已触发");
    }

    /**
     * 统计数据（给仪表盘用）
     */
    @GetMapping("/stats")
    public Result<?> stats() {
        checkAdmin();
        return Result.ok(alertService.getStats());
    }

    // ============================================================
    // 用户接口（关怀推荐，不显示"被预警"字眼）
    // ============================================================

    /**
     * 获取自己的关怀推荐文章
     * <p>
     * 设计要点：用户只能看到推荐文章，看不到"被预警"的字眼
     * 前端文案为"根据你最近的情绪记录，为你推荐以下内容"
     */
    @GetMapping("/my")
    public Result<?> myRecommendedArticles() {
        Long userId = getCurrentUserId();
        List<KnowledgeArticle> articles = alertService.getMyRecommendedArticles(userId);
        return Result.ok(articles);
    }

    // ============================================================
    // 工具方法
    // ============================================================

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
            throw new BusionessException("权限不足");
        }
    }
}
