package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

/**
 * 菜品分类实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("category")
public class Category {
    private Long id;
    private String categoryName;  // 数据库列：category-name
    private Integer sort;
    private LocalDateTime createTime;  // 数据库列：create-time
}
