-- 微信点餐系统数据库SQL
-- 完整的建库和建表语句

-- ============================================================
-- 创建数据库
-- ============================================================
CREATE DATABASE IF NOT EXISTS wechat_ordering DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 使用数据库
USE wechat_ordering;

-- ============================================================
-- 1. 管理员表：t_admin
-- ============================================================
CREATE TABLE IF NOT EXISTS t_admin (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录账号',
  password VARCHAR(100) NOT NULL COMMENT '登录密码',
  `real-name` VARCHAR(50) COMMENT '真实姓名',
  phone VARCHAR(20) COMMENT '联系电话',
  `create-time` DATETIME COMMENT '创建时间',
  INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ============================================================
-- 2. 用户表：t_user
-- ============================================================
CREATE TABLE IF NOT EXISTS t_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
  `open-id` VARCHAR(100) NOT NULL UNIQUE COMMENT '微信OpenID',
  `nick-name` VARCHAR(50) COMMENT '用户昵称',
  avatar VARCHAR(500) COMMENT '头像地址',
  gender TINYINT COMMENT '性别',
  phone VARCHAR(20) UNIQUE COMMENT '手机号',
  status TINYINT COMMENT '账号状态',
  `create-time` DATETIME COMMENT '注册时间',
  INDEX idx_open_id (`open-id`),
  INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 3. 菜品分类表：t_category
-- ============================================================
CREATE TABLE IF NOT EXISTS t_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
  `category-name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  sort INT COMMENT '排序号',
  `create-time` DATETIME COMMENT '创建时间',
  INDEX idx_category_name (`category-name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品分类表';

-- ============================================================
-- 4. 菜品表：t_dish
-- ============================================================
CREATE TABLE IF NOT EXISTS t_dish (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '菜品ID',
  `category-id` BIGINT NOT NULL COMMENT '分类ID',
  `dish-name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  image VARCHAR(500) COMMENT '图片地址',
  price DECIMAL(10,2) NOT NULL COMMENT '价格',
  description TEXT COMMENT '菜品介绍',
  sales INT COMMENT '销量',
  stock INT COMMENT '库存',
  status TINYINT COMMENT '上架状态',
  discount VARCHAR(100) COMMENT '折扣信息',
  `create-time` DATETIME COMMENT '创建时间',
  FOREIGN KEY (`category-id`) REFERENCES t_category(id) ON DELETE CASCADE,
  INDEX idx_category_id (`category-id`),
  INDEX idx_dish_name (`dish-name`),
  INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜品表';

-- ============================================================
-- 5. 收货地址表：t_address
-- ============================================================
CREATE TABLE IF NOT EXISTS t_address (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
  `user-id` BIGINT NOT NULL COMMENT '用户ID',
  receiver VARCHAR(50) NOT NULL COMMENT '收货人',
  phone VARCHAR(20) NOT NULL COMMENT '联系电话',
  `address-detail` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is-default` TINYINT COMMENT '默认地址',
  `create-time` DATETIME COMMENT '创建时间',
  FOREIGN KEY (`user-id`) REFERENCES t_user(id) ON DELETE CASCADE,
  INDEX idx_user_id (`user-id`),
  INDEX idx_is_default (`is-default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- ============================================================
-- 6. 购物车表：t_cart
-- ============================================================
CREATE TABLE IF NOT EXISTS t_cart (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '购物车ID',
  `user-id` BIGINT NOT NULL COMMENT '用户ID',
  `dish-id` BIGINT NOT NULL COMMENT '菜品ID',
  quantity INT NOT NULL COMMENT '数量',
  `total-price` DECIMAL(10,2) COMMENT '总价',
  `create-time` DATETIME COMMENT '添加时间',
  FOREIGN KEY (`user-id`) REFERENCES t_user(id) ON DELETE CASCADE,
  FOREIGN KEY (`dish-id`) REFERENCES t_dish(id) ON DELETE CASCADE,
  INDEX idx_user_id (`user-id`),
  INDEX idx_dish_id (`dish-id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================================
-- 7. 订单表：t_order
-- ============================================================
CREATE TABLE IF NOT EXISTS t_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
  `order-no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
  `user-id` BIGINT NOT NULL COMMENT '用户ID',
  `total-amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `pay-amount` DECIMAL(10,2) COMMENT '实付金额',
  `pay-status` TINYINT COMMENT '支付状态',
  `order-status` TINYINT COMMENT '订单状态',
  remark VARCHAR(255) COMMENT '订单备注',
  `address-id` BIGINT COMMENT '收货地址ID',
  receiver VARCHAR(50) COMMENT '收货人',
  `receiver-phone` VARCHAR(20) COMMENT '联系电话',
  `create-time` DATETIME COMMENT '下单时间',
  FOREIGN KEY (`user-id`) REFERENCES t_user(id) ON DELETE RESTRICT,
  FOREIGN KEY (`address-id`) REFERENCES t_address(id) ON DELETE SET NULL,
  INDEX idx_user_id (`user-id`),
  INDEX idx_order_no (`order-no`),
  INDEX idx_order_status (`order-status`),
  INDEX idx_create_time (`create-time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 8. 订单详情表：t_order_detail
-- ============================================================
CREATE TABLE IF NOT EXISTS t_order_detail (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '详情ID',
  `order-id` BIGINT NOT NULL COMMENT '订单ID',
  `dish-id` BIGINT NOT NULL COMMENT '菜品ID',
  `dish-name` VARCHAR(100) NOT NULL COMMENT '菜品名称',
  price DECIMAL(10,2) NOT NULL COMMENT '单价',
  quantity INT NOT NULL COMMENT '数量',
  amount DECIMAL(10,2) NOT NULL COMMENT '小计',
  `create-time` DATETIME COMMENT '创建时间',
  FOREIGN KEY (`order-id`) REFERENCES t_order(id) ON DELETE CASCADE,
  FOREIGN KEY (`dish-id`) REFERENCES t_dish(id) ON DELETE RESTRICT,
  INDEX idx_order_id (`order-id`),
  INDEX idx_dish_id (`dish-id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单详情表';

-- ============================================================
-- 9. 支付记录表：t_payment
-- ============================================================
CREATE TABLE IF NOT EXISTS t_payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '支付ID',
  `order-id` BIGINT NOT NULL UNIQUE COMMENT '订单ID',
  `pay-no` VARCHAR(100) NOT NULL COMMENT '支付流水号',
  `pay-amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `pay-method` VARCHAR(20) COMMENT '支付方式',
  `pay-time` DATETIME COMMENT '支付时间',
  `create-time` DATETIME COMMENT '创建时间',
  FOREIGN KEY (`order-id`) REFERENCES t_order(id) ON DELETE CASCADE,
  INDEX idx_order_id (`order-id`),
  INDEX idx_pay_no (`pay-no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表';

-- ============================================================
-- 10. 订单评价表：t_order_comment
-- ============================================================
CREATE TABLE IF NOT EXISTS t_order_comment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
  `order-id` BIGINT NOT NULL COMMENT '关联订单ID',
  `user-id` BIGINT NOT NULL COMMENT '评价用户ID',
  score INT COMMENT '评分',
  content TEXT COMMENT '评价内容',
  photo VARCHAR(500) COMMENT '评价图片地址',
  `create-time` DATETIME COMMENT '评价时间',
  FOREIGN KEY (`order-id`) REFERENCES t_order(id) ON DELETE CASCADE,
  FOREIGN KEY (`user-id`) REFERENCES t_user(id) ON DELETE CASCADE,
  INDEX idx_order_id (`order-id`),
  INDEX idx_user_id (`user-id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单评价表';

-- ============================================================
-- 11. 用户反馈表：t_feedback
-- ============================================================
CREATE TABLE IF NOT EXISTS t_feedback (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '反馈ID',
  `user-id` BIGINT NOT NULL COMMENT '用户ID',
  content TEXT NOT NULL COMMENT '反馈内容',
  `reply-content` TEXT COMMENT '管理员回复',
  status TINYINT COMMENT '处理状态',
  `create-time` DATETIME COMMENT '反馈时间',
  FOREIGN KEY (`user-id`) REFERENCES t_user(id) ON DELETE CASCADE,
  INDEX idx_user_id (`user-id`),
  INDEX idx_status (status),
  INDEX idx_create_time (`create-time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- ============================================================
-- 12. 轮播图表：t_banner
-- ============================================================
CREATE TABLE IF NOT EXISTS t_banner (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '轮播图ID',
  title VARCHAR(100) NOT NULL COMMENT '轮播图标题',
  `image-url` VARCHAR(500) NOT NULL COMMENT '图片路径',
  `link-url` VARCHAR(255) COMMENT '跳转链接',
  sort INT COMMENT '排序号',
  status TINYINT COMMENT '状态（0禁用 1启用）',
  remark TEXT COMMENT '备注',
  `create-time` DATETIME COMMENT '创建时间',
  INDEX idx_status (status),
  INDEX idx_sort (sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='轮播图表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 插入默认管理员
INSERT INTO t_admin (username, password, `real-name`, phone, `create-time`) 
VALUES ('admin', '123456', '管理员', '18888888888', NOW());

-- 插入菜品分类
INSERT INTO t_category (`category-name`, sort, `create-time`) VALUES
('热菜', 1, NOW()),
('冷菜', 2, NOW()),
('汤类', 3, NOW()),
('米饭', 4, NOW()),
('面食', 5, NOW()),
('饮品', 6, NOW());

-- 插入轮播图
INSERT INTO t_banner (title, `image-url`, status, sort, `create-time`) VALUES
('特色菜推荐', 'https://example.com/banner1.jpg', 1, 1, NOW()),
('优惠活动', 'https://example.com/banner2.jpg', 1, 2, NOW()),
('新品上市', 'https://example.com/banner3.jpg', 1, 3, NOW());

-- ============================================================
-- 创建数据库SQL执行完成
-- ============================================================
