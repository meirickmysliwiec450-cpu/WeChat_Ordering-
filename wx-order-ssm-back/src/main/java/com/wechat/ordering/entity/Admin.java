package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

/**
 * 管理员实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("admin")
public class Admin {
    private Long id;
    private String username;
    private String password;
    private String realName;     // 数据库列：real-name
    private String phone;
    private LocalDateTime createTime;  // 数据库列：create-time
}
