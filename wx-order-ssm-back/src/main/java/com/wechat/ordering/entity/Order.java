package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("order")
public class Order {
    private Long id;
    private String orderNo;      // 数据库列：order-no
    private Long userId;         // 数据库列：user-id
    private BigDecimal totalAmount;  // 数据库列：total-amount
    private BigDecimal payAmount;    // 数据库列：pay-amount
    private Integer payStatus;       // 数据库列：pay-status
    private Integer orderStatus;     // 数据库列：order-status
    private String remark;
    private Long addressId;          // 数据库列：address-id
    private String receiver;
    private String receiverPhone;    // 数据库列：receiver-phone
    private LocalDateTime createTime;  // 数据库列：create-time
}
