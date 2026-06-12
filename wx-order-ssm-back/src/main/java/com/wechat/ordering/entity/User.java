package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("user")
public class User {
    private Long id;
    private String openId;      // 数据库列：open-id
    private String nickName;    // 数据库列：nick-name
    private String avatar;
    private Integer gender;
    private String phone;
    private Integer status;
    private LocalDateTime createTime;  // 数据库列：create-time
}
