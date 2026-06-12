package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("cart")
public class Cart {
    private Long id;
    private Long userId;         // 数据库列：user-id
    private Long dishId;         // 数据库列：dish-id
    private Integer quantity;
    private BigDecimal totalPrice;  // 数据库列：total-price
    private LocalDateTime createTime;  // 数据库列：create-time
}
