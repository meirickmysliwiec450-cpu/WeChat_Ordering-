package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

/**
 * 轮播图实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("banner")
public class Banner {
    private Long id;
    private String title;
    private String imageUrl;    // 数据库列：image-url
    private String linkUrl;     // 数据库列：link-url
    private Integer sort;
    private Integer status;
    private String remark;
    private LocalDateTime createTime;  // 数据库列：create-time
}
