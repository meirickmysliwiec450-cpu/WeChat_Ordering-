package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

/**
 * 订单评价实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("orderComment")
public class OrderComment {
    private Long id;
    private Long orderId;        // 数据库列：order-id
    private Long userId;         // 数据库列：user-id
    private Integer score;
    private String content;
    private String photo;
    private LocalDateTime createTime;  // 数据库列：create-time
}
