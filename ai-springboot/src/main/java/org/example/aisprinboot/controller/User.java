package org.example.aisprinboot.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.example.aisprinboot.DTO.command.UserLoginCommandDTO;
import org.example.aisprinboot.DTO.command.UserRegisterCommandDTO;
import org.example.aisprinboot.DTO.response.UserLoginResponseDTO;
import org.example.aisprinboot.common.Result;
import org.example.aisprinboot.service.UserService;
import org.example.aisprinboot.util.JwtTokenUtil;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户控制器：登录、注册、获取当前用户
 *
 * @author PANJU
 */
@RestController
@RequestMapping("/api/user")
public class User {

    @Resource
    private UserService userService;

    /**
     * 用户登录接口
     */
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(@Valid @RequestBody UserLoginCommandDTO commandDTO) {
        // 调用服务层登录方法
        UserLoginResponseDTO result = userService.login(commandDTO);
        return Result.ok(result);
    }

    /**
     * 用户注册接口
     */
    @PostMapping("/add")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO commandDTO) {
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.register(commandDTO);
        return Result.ok(result);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public Result<UserLoginResponseDTO.UserDetailResponseDTO> getCurrentUser() {
        // 从token中解析出用户的id
        String token = JwtTokenUtil.getCurrentToken();
        DecodedJWT jwt = JwtTokenUtil.verifyToken(token);
        Long userId = jwt.getClaim("userId").asLong();
        // 调用service层获取用户详情
        UserLoginResponseDTO.UserDetailResponseDTO result = userService.getUserById(userId);
        return Result.ok(result);
    }
}
