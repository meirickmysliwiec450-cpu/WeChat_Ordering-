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
@Data  //自动生成所有属性的getter和setter方法
@NoArgsConstructor //自动生成无参构造方法
@AllArgsConstructor //自动生成有参构造方法
@Builder //自动生成构建器模式的构造方法
@Alias("address") //指定别名为address，用于MyBatis映射
public class Address {
    private Long id;
    private Long userId;         // 数据库列：user-id
    private String receiver;
    private String phone;
    private String addressDetail;  // 数据库列：address-detail
    private Integer isDefault;      // 数据库列：is-default
    private LocalDateTime createTime;  // 数据库列：create-time
}
