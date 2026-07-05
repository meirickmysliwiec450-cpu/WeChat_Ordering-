package com.wechat.ordering.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 数据库初始化：自动添加营养字段和对话表（如果不存在）
 */
@Component
public class DbInitConfig implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.execute("ALTER TABLE t_dish ADD COLUMN calories INT DEFAULT 0 COMMENT '热量(千卡/100g)' AFTER discount");
        } catch (Exception e) { /* 字段已存在 */ }
        try {
            jdbcTemplate.execute("ALTER TABLE t_dish ADD COLUMN protein DECIMAL(10,1) DEFAULT 0 COMMENT '蛋白质(g/100g)' AFTER calories");
        } catch (Exception e) { /* 字段已存在 */ }
        try {
            jdbcTemplate.execute("ALTER TABLE t_dish ADD COLUMN fat DECIMAL(10,1) DEFAULT 0 COMMENT '脂肪(g/100g)' AFTER protein");
        } catch (Exception e) { /* 字段已存在 */ }
        try {
            jdbcTemplate.execute("ALTER TABLE t_dish ADD COLUMN carbs DECIMAL(10,1) DEFAULT 0 COMMENT '碳水化合物(g/100g)' AFTER fat");
        } catch (Exception e) { /* 字段已存在 */ }
        try {
            jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS t_conversation (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "`user-id` BIGINT, role VARCHAR(20), content TEXT, `create-time` DATETIME)");
        } catch (Exception e) { /* 表已存在 */ }
        // 超级管理员字段
        try {
            jdbcTemplate.execute("ALTER TABLE t_admin ADD COLUMN role VARCHAR(20) DEFAULT 'admin' AFTER phone");
        } catch (Exception e) { /* 已存在 */ }
        try {
            jdbcTemplate.execute("ALTER TABLE t_admin ADD COLUMN status INT DEFAULT 1 AFTER role");
        } catch (Exception e) { /* 已存在 */ }
        // 确保默认admin是超级管理员
        try {
            jdbcTemplate.update("UPDATE t_admin SET role='super_admin', status=1 WHERE username='admin'");
        } catch (Exception e) { /* 忽略 */ }
        
        // ========== 自动添加示例饮品数据 ==========
        try {
            initSampleDrinks();
        } catch (Exception e) {
            System.out.println("添加饮品数据时出错：" + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 初始化示例饮品数据
     */
    private void initSampleDrinks() {
        // 1. 先查询饮品分类ID
        List<Map<String, Object>> categories = jdbcTemplate.queryForList(
            "SELECT id FROM t_category WHERE `category-name` = '饮品'");
        
        if (categories.isEmpty()) {
            System.out.println("未找到饮品分类，先创建分类...");
            jdbcTemplate.update(
                "INSERT INTO t_category (`category-name`, sort, `create-time`) VALUES (?, ?, NOW())",
                "饮品", 6);
            categories = jdbcTemplate.queryForList(
                "SELECT id FROM t_category WHERE `category-name` = '饮品'");
        }
        
        Long categoryId = ((Number) categories.get(0).get("id")).longValue();
        System.out.println("饮品分类ID: " + categoryId);
        
        // 2. 检查是否已有饮品数据
        List<Map<String, Object>> existingDrinks = jdbcTemplate.queryForList(
            "SELECT id FROM t_dish WHERE `category-id` = ?", categoryId);
        
        if (!existingDrinks.isEmpty()) {
            System.out.println("数据库中已有 " + existingDrinks.size() + " 个饮品，跳过添加");
            return;
        }
        
        // 3. 添加示例饮品
        System.out.println("开始添加示例饮品数据...");
        
        // 冰红茶
        try {
            jdbcTemplate.update(
                "INSERT INTO t_dish (`category-id`, `dish-name`, image, price, description, sales, stock, status, `create-time`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                categoryId, "冰红茶", "/images/drinks/ice_tea.png", 6.00, "经典冰红茶，清凉解暑", 0, 100, 1);
            System.out.println("✓ 已添加：冰红茶");
        } catch (Exception e) {
            System.out.println("添加冰红茶失败：" + e.getMessage());
        }
        
        // 多肉葡萄冰萃
        try {
            jdbcTemplate.update(
                "INSERT INTO t_dish (`category-id`, `dish-name`, image, price, description, sales, stock, status, `create-time`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                categoryId, "多肉葡萄冰萃", "/images/drinks/grape_drink.png", 18.00, "新鲜葡萄，多肉多汁", 0, 50, 1);
            System.out.println("✓ 已添加：多肉葡萄冰萃");
        } catch (Exception e) {
            System.out.println("添加多肉葡萄冰萃失败：" + e.getMessage());
        }
        
        // 美年达
        try {
            jdbcTemplate.update(
                "INSERT INTO t_dish (`category-id`, `dish-name`, image, price, description, sales, stock, status, `create-time`) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW())",
                categoryId, "美年达", "/images/drinks/mirinda.png", 5.00, "橙味汽水，清爽解渴", 0, 80, 1);
            System.out.println("✓ 已添加：美年达");
        } catch (Exception e) {
            System.out.println("添加美年达失败：" + e.getMessage());
        }
        
        // 4. 验证添加结果
        List<Map<String, Object>> drinks = jdbcTemplate.queryForList(
            "SELECT `dish-name`, price FROM t_dish WHERE `category-id` = ?", categoryId);
        System.out.println("饮品数据添加完成！共 " + drinks.size() + " 个饮品：");
        for (Map<String, Object> drink : drinks) {
            System.out.println("  - " + drink.get("dish-name") + " ¥" + drink.get("price"));
        }
    }
}
