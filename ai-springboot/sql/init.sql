-- =====================================================
-- 心理健康助手 - 数据库初始化脚本
-- 使用方式：在 DataGrip 中连接 MySQL 后，打开本文件全部执行
-- 对应配置：application.yml -> jdbc:mysql://localhost:3306/mental_health_assistant
-- =====================================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS mental_health_assistant
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE mental_health_assistant;

-- 2. 用户表（对应实体 org.example.aisprinboot.entity.User）
CREATE TABLE IF NOT EXISTS `user` (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名（字母数字下划线）',
    email       VARCHAR(100) NOT NULL COMMENT '邮箱',
    phone       VARCHAR(20)           COMMENT '手机号',
    password    VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    nickname    VARCHAR(50)           COMMENT '昵称',
    avatar      VARCHAR(255)          COMMENT '头像路径',
    gender      INT                   COMMENT '性别 1:男 2:女',
    birthday    DATE                  COMMENT '生日',
    user_type   INT          NOT NULL DEFAULT 1 COMMENT '用户类型 1:普通用户 2:管理员',
    status      INT          NOT NULL DEFAULT 1 COMMENT '状态 0:禁用 1:正常',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_email (email)
) ENGINE = InnoDB COMMENT = '用户表';

-- 3. 咨询会话表（对应实体 ConsultationSession）
CREATE TABLE IF NOT EXISTS consultation_session (
    id                      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    user_id                 BIGINT      NOT NULL COMMENT '用户ID',
    session_title           VARCHAR(200)         COMMENT '会话标题',
    started_at              DATETIME             COMMENT '开始时间',
    last_emotion_analysis   TEXT                 COMMENT '最后一次情绪分析结果(JSON)',
    last_emotion_updated_at DATETIME             COMMENT '最后一次情绪分析更新时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    CONSTRAINT fk_session_user FOREIGN KEY (user_id) REFERENCES `user` (id)
) ENGINE = InnoDB COMMENT = '咨询会话表';

-- 4. 咨询消息表（对应实体 ConsultationMessage）
CREATE TABLE IF NOT EXISTS consultation_message (
    id          BIGINT     NOT NULL AUTO_INCREMENT COMMENT '消息ID',
    session_id  BIGINT     NOT NULL COMMENT '会话ID',
    sender_type INT        NOT NULL COMMENT '发送者类型 1:用户 2:AI助手',
    message_type INT       NOT NULL DEFAULT 1 COMMENT '消息类型 1:文本',
    content     TEXT       NOT NULL COMMENT '消息内容',
    emotion_tag VARCHAR(50)         COMMENT '情绪标签',
    ai_model    VARCHAR(50)         COMMENT '使用的AI模型',
    created_at  DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id),
    KEY idx_session_id (session_id),
    CONSTRAINT fk_message_session FOREIGN KEY (session_id) REFERENCES consultation_session (id)
) ENGINE = InnoDB COMMENT = '咨询消息表';

-- 5. 验证：查看创建的表
SHOW TABLES;
