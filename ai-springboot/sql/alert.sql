-- ============================================================
-- 危机预警模块建表脚本
-- 依赖已存在的表：user, emotion_diary, knowledge_article
-- @author PANJU
-- ============================================================

-- 1. 预警记录表
CREATE TABLE IF NOT EXISTS alert_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    diary_id BIGINT NOT NULL COMMENT '触发的日记ID',
    alert_level INT NOT NULL COMMENT '1低危 2中危 3高危',
    trigger_reason TEXT COMMENT 'JSON数组，命中的规则列表',
    ai_analysis TEXT COMMENT 'AI深度分析结果',
    recommended_articles TEXT COMMENT 'JSON数组，推荐文章UUID',
    status INT NOT NULL DEFAULT 0 COMMENT '0未处理 1已处理 2已忽略',
    handled_by BIGINT COMMENT '处理人ID',
    handled_at DATETIME COMMENT '处理时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_diary (user_id, diary_id),
    KEY idx_user_status (user_id, status),
    KEY idx_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='危机预警记录';

-- 2. 管理员通知表
CREATE TABLE IF NOT EXISTS alert_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    alert_id BIGINT NOT NULL COMMENT '关联预警ID',
    admin_id BIGINT NOT NULL COMMENT '接收管理员ID',
    is_read TINYINT NOT NULL DEFAULT 0 COMMENT '0未读 1已读',
    read_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_alert (alert_id),
    KEY idx_admin_read (admin_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员预警通知';

-- 3. 预警关键词表
CREATE TABLE IF NOT EXISTS alert_keyword (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    keyword VARCHAR(50) NOT NULL,
    category VARCHAR(30) NOT NULL COMMENT 'SUICIDE/SELF_HARM/VIOLENCE/OTHER',
    risk_weight INT DEFAULT 1 COMMENT '命中权重',
    status INT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_keyword (keyword)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警关键词配置';

-- 初始关键词数据
INSERT INTO alert_keyword (keyword, category, risk_weight) VALUES
('自杀','SUICIDE',10), ('不想活','SUICIDE',10), ('结束生命','SUICIDE',10),
('活着没意思','SUICIDE',8), ('想死','SUICIDE',10), ('轻生','SUICIDE',9),
('自残','SELF_HARM',8), ('割腕','SELF_HARM',9), ('伤害自己','SELF_HARM',7),
('打人','VIOLENCE',5), ('报复','VIOLENCE',6), ('毁灭','VIOLENCE',7)
ON DUPLICATE KEY UPDATE keyword=VALUES(keyword);
