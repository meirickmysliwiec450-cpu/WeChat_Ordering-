package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("dish")
public class Dish {
    private Long id;
    private Long categoryId;     // 数据库列：category-id
    private String dishName;     // 数据库列：dish-name
    private String image;
    private BigDecimal price;
    private String description;
    private Integer sales;
    private Integer stock;
    private Integer status;
    private String discount;
    private LocalDateTime createTime;  // 数据库列：create-time
}
