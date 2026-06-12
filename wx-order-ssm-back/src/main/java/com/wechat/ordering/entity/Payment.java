package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("payment")
public class Payment {
    private Long id;
    private Long orderId;        // 数据库列：order-id
    private String payNo;        // 数据库列：pay-no
    private BigDecimal payAmount;  // 数据库列：pay-amount
    private String payMethod;      // 数据库列：pay-method
    private LocalDateTime payTime;  // 数据库列：pay-time
    private LocalDateTime createTime;  // 数据库列：create-time
}
