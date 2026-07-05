-- 专门更新饮品的图片路径为英文路径
USE wechat_ordering;

-- 查看当前饮品的图片路径
SELECT id, `dish-name`, image FROM t_dish WHERE `category-id` IN (
    SELECT id FROM t_category WHERE `category-name` = '饮品'
);

-- 更新饮品的图片路径
UPDATE t_dish SET image = '/images/drinks/ice_tea.png' WHERE `dish-name` LIKE '%冰红茶%';
UPDATE t_dish SET image = '/images/drinks/grape_drink.png' WHERE `dish-name` LIKE '%葡萄%';
UPDATE t_dish SET image = '/images/drinks/mirinda.png' WHERE `dish-name` LIKE '%美年达%';

-- 查看更新后的结果
SELECT id, `dish-name`, image FROM t_dish WHERE `category-id` IN (
    SELECT id FROM t_category WHERE `category-name` = '饮品'
);
