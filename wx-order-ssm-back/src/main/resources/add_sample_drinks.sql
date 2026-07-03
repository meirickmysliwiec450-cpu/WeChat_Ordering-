-- 添加示例饮品数据
USE wechat_ordering;

-- 先查询饮品分类的ID（应该是6）
-- INSERT INTO t_dish (`category-id`, `dish-name`, image, price, description, sales, stock, status, `create-time`) VALUES
-- (6, '冰红茶', '/images/饮品/冰红茶.png', 6.00, '经典冰红茶，清凉解暑', 0, 100, 1, NOW()),
-- (6, '多肉葡萄冰萃', '/images/饮品/多肉葡萄冰萃.png', 18.00, '新鲜葡萄，多肉多汁', 0, 50, 1, NOW()),
-- (6, '美年达', '/images/饮品/美年达.png', 5.00, '橙味汽水，清爽解渴', 0, 80, 1, NOW());

-- 更安全的方式：先获取分类ID再插入
INSERT INTO t_dish (`category-id`, `dish-name`, image, price, description, sales, stock, status, `create-time`) 
SELECT id, '冰红茶', '/images/饮品/冰红茶.png', 6.00, '经典冰红茶，清凉解暑', 0, 100, 1, NOW() 
FROM t_category WHERE `category-name` = '饮品';

INSERT INTO t_dish (`category-id`, `dish-name`, image, price, description, sales, stock, status, `create-time`) 
SELECT id, '多肉葡萄冰萃', '/images/饮品/多肉葡萄冰萃.png', 18.00, '新鲜葡萄，多肉多汁', 0, 50, 1, NOW() 
FROM t_category WHERE `category-name` = '饮品';

INSERT INTO t_dish (`category-id`, `dish-name`, image, price, description, sales, stock, status, `create-time`) 
SELECT id, '美年达', '/images/饮品/美年达.png', 5.00, '橙味汽水，清爽解渴', 0, 80, 1, NOW() 
FROM t_category WHERE `category-name` = '饮品';

-- 查询确认
SELECT d.id, c.`category-name`, d.`dish-name`, d.price, d.image, d.status 
FROM t_dish d 
JOIN t_category c ON d.`category-id` = c.id 
WHERE c.`category-name` = '饮品';
