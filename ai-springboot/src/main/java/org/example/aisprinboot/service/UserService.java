package org.example.aisprinboot.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.aisprinboot.DTO.command.UserLoginCommandDTO;
import org.example.aisprinboot.DTO.command.UserRegisterCommandDTO;
import org.example.aisprinboot.DTO.response.UserLoginResponseDTO;
import org.example.aisprinboot.entity.User;
import org.example.aisprinboot.enumClass.UserType;
import org.example.aisprinboot.exception.BusionessException;
import org.example.aisprinboot.mapper.UserMapper;
import org.example.aisprinboot.service.convert.UserConvert;
import org.example.aisprinboot.util.JwtTokenUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户业务层：登录、注册、查询
 *
 * @author PANJU
 */
@Slf4j
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 密码加密器
     */
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     *
     * @param commandDTO 登录入参
     * @return 登录响应（token + 用户信息）
     */
    public UserLoginResponseDTO login(UserLoginCommandDTO commandDTO) {
        // 构建查询条件：用户名或邮箱
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, commandDTO.getUsername())
                .or()
                .eq(User::getEmail, commandDTO.getUsername());
        // 调用MP API查询
        User user = userMapper.selectOne(queryWrapper);
        log.debug("登录查询用户结果: {}", user);

        // 判断用户是否存在
        if (user == null) {
            throw new BusionessException("用户不存在");
        }
        // 验证密码
        String inputPassword = commandDTO.getPassword().trim();
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            throw new BusionessException("密码错误");
        }

        // 检查用户的状态
        if (!user.isActive()) {
            throw new BusionessException("用户已被禁用，请联系管理员");
        }

        // 生成JWT token
        String token = JwtTokenUtil.generateToken(user.getId(), user.getUsername(), user.getUserType());
        UserLoginResponseDTO.UserDetailResponseDTO userInfo = UserConvert.entityToDetailResponse(user);
        return UserConvert.entityToLoginResponse(token, userInfo);
    }

    /**
     * 用户注册
     *
     * @param commandDTO 注册入参
     * @return 注册后的用户详情
     */
    public UserLoginResponseDTO.UserDetailResponseDTO register(UserRegisterCommandDTO commandDTO) {
        log.debug("用户注册入参: {}", JSONUtil.parseObj(commandDTO));
        // 验证密码是否一致
        if (!commandDTO.getPassword().equals(commandDTO.getConfirmPassword())) {
            throw new BusionessException("两次输入密码不一致");
        }

        // 检查用户名是否存在
        LambdaQueryWrapper<User> userNameQuery = new LambdaQueryWrapper<>();
        userNameQuery.eq(User::getUsername, commandDTO.getUsername());
        if (userMapper.selectCount(userNameQuery) > 0) {
            throw new BusionessException("用户名已存在");
        }

        // 检查邮箱是否存在
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, commandDTO.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusionessException("邮箱已存在");
        }

        // 校验用户类型
        if (!UserType.isValidCode(commandDTO.getUserType())) {
            throw new BusionessException("无效的用户类型");
        }

        // 创建用户（密码BCrypt加密）
        String password = commandDTO.getPassword().trim();
        String encodedPassword = passwordEncoder.encode(password);
        User user = UserConvert.registerCommandToEntity(commandDTO, encodedPassword);

        // 插入数据库
        userMapper.insert(user);

        return UserConvert.entityToDetailResponse(user);
    }

    /**
     * 根据ID获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情
     */
    public UserLoginResponseDTO.UserDetailResponseDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusionessException("用户不存在");
        }
        return UserConvert.entityToDetailResponse(user);
    }
}
