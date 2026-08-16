package org.example.aisprinboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.aisprinboot.common.Result;
import org.example.aisprinboot.entity.KnowledgeArticle;
import org.example.aisprinboot.entity.User;
import org.example.aisprinboot.service.KnowledgeArticleService;
import org.example.aisprinboot.service.KnowledgeCategoryService;
import org.example.aisprinboot.mapper.UserMapper;
import org.example.aisprinboot.util.JwtTokenUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 知识库控制器
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeController {

    @Autowired
    private KnowledgeCategoryService categoryService;

    @Autowired
    private KnowledgeArticleService articleService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取分类树
     */
    @GetMapping("/category/tree")
    public Result<?> getCategoryTree() {
        return Result.ok(categoryService.listTree());
    }

    /**
     * 分页获取文章列表
     */
    @GetMapping("/article/page")
    public Result<?> getArticlePage(
            @RequestParam(defaultValue = "1") Integer currentPage,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "publishedAt") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

        Page<KnowledgeArticle> page = new Page<>(currentPage, size);
        Map<String, Object> params = new HashMap<>();
        if (categoryId != null) params.put("categoryId", categoryId);
        if (keyword != null) params.put("keyword", keyword);
        if (status != null) params.put("status", status);
        params.put("sortField", sortField);
        params.put("sortDirection", sortDirection);

        // 判断是否是管理员（如果有 token 且是管理员）
        boolean isAdmin = false;
        try {
            String token = JwtTokenUtil.getCurrentToken();
            if (token != null && !token.isEmpty()) {
                DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
                if (jwt != null) {
                    Long userId = jwt.getClaim("userId").asLong();
                    User user = userMapper.selectById(userId);
                    if (user != null && user.getUserType() != null && user.getUserType() == 2) {
                        isAdmin = true;
                    }
                }
            }
        } catch (Exception e) {
            // 忽略异常，视为普通用户
        }

        return Result.ok(articleService.getPage(page, params, isAdmin));
    }

    /**
     * 获取文章详情
     */
    @GetMapping("/article/{id}")
    public Result<?> getArticleDetail(@PathVariable String id) {
        return Result.ok(articleService.getById(id));
    }

    /**
     * 创建文章（管理员）
     */
    @PostMapping("/article")
    public Result<?> createArticle(@RequestBody KnowledgeArticle article) {
        Long userId = getCurrentUserId();
        return Result.ok(articleService.create(article, userId));
    }

    /**
     * 更新文章（管理员）
     */
    @PutMapping("/article/{id}")
    public Result<?> updateArticle(@PathVariable String id, @RequestBody KnowledgeArticle article) {
        return Result.ok(articleService.update(id, article));
    }

    /**
     * 更新文章状态（管理员）
     */
    @PutMapping("/article/{id}/status")
    public Result<?> changeStatus(@PathVariable String id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        articleService.changeStatus(id, status);
        return Result.ok();
    }

    /**
     * 删除文章（管理员）
     */
    @DeleteMapping("/article/{id}")
    public Result<?> deleteArticle(@PathVariable String id) {
        articleService.delete(id);
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
}
