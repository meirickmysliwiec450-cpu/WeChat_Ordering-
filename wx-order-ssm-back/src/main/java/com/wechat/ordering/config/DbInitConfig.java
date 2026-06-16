package com.wechat.ordering.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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
    }
}
