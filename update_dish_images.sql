-- 统一更新所有菜品的图片路径为英文路径
-- 使用前请先备份数据库！

USE wechat_ordering;

-- 先查看当前的菜品数据
SELECT id, dish_name, image FROM dish;

-- 更新所有菜品的图片路径为英文路径
-- 冷菜
UPDATE dish SET image = '/images/colddishes/garlic_pork.png' WHERE dish_name LIKE '%蒜泥白肉%';

-- 热菜
UPDATE dish SET image = '/images/hotdishes/braised_pork.png' WHERE dish_name LIKE '%红烧肉%';
UPDATE dish SET image = '/images/hotdishes/chicken_wings.png' WHERE dish_name LIKE '%鸡翅%';

-- 饮品
UPDATE dish SET image = '/images/drinks/ice_tea.png' WHERE dish_name LIKE '%冰红茶%';
UPDATE dish SET image = '/images/drinks/grape_drink.png' WHERE dish_name LIKE '%葡萄%';
UPDATE dish SET image = '/images/drinks/mirinda.png' WHERE dish_name LIKE '%美年达%';

-- 面食
UPDATE dish SET image = '/images/noodles/beef_noodles.png' WHERE dish_name LIKE '%牛肉%';
UPDATE dish SET image = '/images/noodles/dumplings.png' WHERE dish_name LIKE '%饺子%';
UPDATE dish SET image = '/images/noodles/steamed_bun.png' WHERE dish_name LIKE '%馒头%';

-- 米饭
UPDATE dish SET image = '/images/rice/claypot_rice.png' WHERE dish_name LIKE '%煲仔饭%';
UPDATE dish SET image = '/images/rice/yangzhou_rice.png' WHERE dish_name LIKE '%扬州炒饭%';

-- 汤类
UPDATE dish SET image = '/images/soups/tomato_brisket_soup.png' WHERE dish_name LIKE '%番茄牛腩%';
UPDATE dish SET image = '/images/soups/yanduxian_soup.png' WHERE dish_name LIKE '%腌笃鲜%';

-- 查看更新后的结果
SELECT id, dish_name, image FROM dish;
