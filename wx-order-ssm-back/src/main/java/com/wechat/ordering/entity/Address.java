package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

/**
 * 地址实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("address")
public class Address {
    private Long id;
    private Long userId;         // 数据库列：user-id
    private String receiver;
    private String phone;
    private String addressDetail;  // 数据库列：address-detail
    private Integer isDefault;      // 数据库列：is-default
    private LocalDateTime createTime;  // 数据库列：create-time
}
