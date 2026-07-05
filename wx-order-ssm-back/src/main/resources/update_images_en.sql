-- ============================================================
-- 将菜品 + 轮播图图片路径从中文改为英文
-- 在 MySQL 客户端（Navicat/HeidiSQL/命令行）中执行
-- ============================================================

-- ==================== 轮播图表 t_banner ====================
UPDATE t_banner SET `image-url` = '/images/drinks/grape_drink.png'    WHERE `image-url` LIKE '%多肉葡萄冰萃%';
UPDATE t_banner SET `image-url` = '/images/colddishes/garlic_pork.png' WHERE `image-url` LIKE '%蒜泥白肉%';

-- 验证轮播图
SELECT id, title, `image-url` FROM t_banner;

-- ==================== 菜品表 t_dish ====================

USE wechat_ordering;

-- 先备份查看当前图片路径
SELECT id, `dish-name`, image FROM t_dish WHERE image IS NOT NULL AND image != '';

-- 面食 → noodles
UPDATE t_dish SET image = '/images/noodles/dumplings.png'        WHERE image LIKE '%饺子%';
UPDATE t_dish SET image = '/images/noodles/beef_noodles.png'     WHERE image LIKE '%招牌牛肉面%';
UPDATE t_dish SET image = '/images/noodles/steamed_bun.png'      WHERE image LIKE '%馒头%';

-- 热菜 → hotdishes
UPDATE t_dish SET image = '/images/hotdishes/braised_pork.png'   WHERE image LIKE '%经典红烧肉%';
UPDATE t_dish SET image = '/images/hotdishes/chicken_wings.png'  WHERE image LIKE '%鸡翅%';

-- 冷菜 → colddishes
UPDATE t_dish SET image = '/images/colddishes/garlic_pork.png'   WHERE image LIKE '%蒜泥白肉%';

-- 汤类 → soups
UPDATE t_dish SET image = '/images/soups/tomato_brisket_soup.png' WHERE image LIKE '%番茄牛腩汤%';
UPDATE t_dish SET image = '/images/soups/yanduxian_soup.png'     WHERE image LIKE '%腌笃鲜%';

-- 米饭 → rice
UPDATE t_dish SET image = '/images/rice/claypot_rice.png'        WHERE image LIKE '%腊味煲仔饭%';
UPDATE t_dish SET image = '/images/rice/yangzhou_rice.png'       WHERE image LIKE '%扬州炒饭%';

-- 饮品 → drinks
UPDATE t_dish SET image = '/images/drinks/ice_tea.png'           WHERE image LIKE '%冰红茶%';
UPDATE t_dish SET image = '/images/drinks/grape_drink.png'       WHERE image LIKE '%多肉葡萄冰萃%';
UPDATE t_dish SET image = '/images/drinks/mirinda.png'           WHERE image LIKE '%美年达%';

-- 验证更新结果
SELECT id, `dish-name`, image FROM t_dish WHERE image IS NOT NULL AND image != '';
