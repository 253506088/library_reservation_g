-- 图书馆座位预约信息管理系统数据库设计
-- 数据库名称: library_reservation

CREATE DATABASE IF NOT EXISTS library_reservation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE library_reservation;

-- 1. 用户表
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(32) NOT NULL COMMENT '密码(MD5加密)',
    `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
    `phone` VARCHAR(11) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `user_type` VARCHAR(10) NOT NULL DEFAULT '学生' COMMENT '用户类型: 学生, 管理员',
    `status` VARCHAR(10) NOT NULL DEFAULT '正常' COMMENT '状态: 正常, 禁用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_user_type` (`user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2. 图书馆表
CREATE TABLE `library` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '图书馆ID',
    `name` VARCHAR(100) NOT NULL COMMENT '图书馆名称',
    `address` VARCHAR(200) COMMENT '地址',
    `description` TEXT COMMENT '描述',
    `status` VARCHAR(10) NOT NULL DEFAULT '正常' COMMENT '状态: 正常, 停用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书馆表';

-- 3. 座位表
CREATE TABLE `seat` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '座位ID',
    `library_id` BIGINT NOT NULL COMMENT '图书馆ID',
    `seat_number` VARCHAR(20) NOT NULL COMMENT '座位编号',
    `seat_type` VARCHAR(20) DEFAULT '普通座位' COMMENT '座位类型',
    `status` VARCHAR(10) NOT NULL DEFAULT '正常' COMMENT '状态: 正常, 维修, 停用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_library_seat` (`library_id`, `seat_number`),
    KEY `idx_library_id` (`library_id`),
    KEY `idx_status` (`status`),
    CONSTRAINT `fk_seat_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位表';

-- 4. 预约记录表
CREATE TABLE `reservation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预约ID',
    `order_no` VARCHAR(20) NOT NULL COMMENT '预约流水号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `library_id` BIGINT NOT NULL COMMENT '图书馆ID',
    `seat_id` BIGINT NOT NULL COMMENT '座位ID',
    `start_time` DATETIME NOT NULL COMMENT '预约开始时间',
    `end_time` DATETIME NOT NULL COMMENT '预约结束时间',
    `status` VARCHAR(10) NOT NULL DEFAULT '已预约' COMMENT '状态: 已预约, 已使用, 爽约, 已取消',
    `qr_code` TEXT COMMENT '二维码内容',
    `check_in_time` DATETIME COMMENT '签到时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_library_id` (`library_id`),
    KEY `idx_seat_id` (`seat_id`),
    KEY `idx_start_end_time` (`start_time`, `end_time`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`),
    CONSTRAINT `fk_reservation_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_reservation_library` FOREIGN KEY (`library_id`) REFERENCES `library` (`id`),
    CONSTRAINT `fk_reservation_seat` FOREIGN KEY (`seat_id`) REFERENCES `seat` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='预约记录表';

-- 5. 系统配置表（用于存储流水号等配置）
CREATE TABLE `system_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '配置ID',
    `config_key` VARCHAR(50) NOT NULL COMMENT '配置键',
    `config_value` VARCHAR(200) NOT NULL COMMENT '配置值',
    `description` VARCHAR(200) COMMENT '描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 插入初始数据
-- 管理员用户
INSERT INTO `user` (`username`, `password`, `real_name`, `user_type`) VALUES 
('admin', MD5('123456'), '系统管理员', '管理员');

-- 测试学生用户
INSERT INTO `user` (`username`, `password`, `real_name`, `user_type`) VALUES 
('student001', MD5('123456'), '张三', '学生'),
('student002', MD5('123456'), '李四', '学生');

-- 测试图书馆
INSERT INTO `library` (`name`, `address`, `description`) VALUES 
('中央图书馆', '校园中心区域', '主要图书馆，藏书丰富'),
('理工图书馆', '理工学院大楼', '专业图书馆，主要收藏理工类书籍');

-- 测试座位
INSERT INTO `seat` (`library_id`, `seat_number`, `seat_type`) VALUES 
(1, '001', '普通座位'),
(1, '002', '普通座位'),
(1, '003', '电脑座位'),
(2, '001', '普通座位'),
(2, '002', '普通座位');

-- 系统配置初始化
INSERT INTO `system_config` (`config_key`, `config_value`, `description`) VALUES 
('daily_order_sequence', '0', '每日预约流水号序列');