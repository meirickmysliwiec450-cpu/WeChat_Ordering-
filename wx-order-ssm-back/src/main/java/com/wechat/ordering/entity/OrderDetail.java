package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单详情实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("orderDetail")
public class OrderDetail {
    private Long id;
    private Long orderId;        // 数据库列：order-id
    private Long dishId;         // 数据库列：dish-id
    private String dishName;     // 数据库列：dish-name
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal amount;
    private LocalDateTime createTime;  // 数据库列：create-time
}
