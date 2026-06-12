package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

/**
 * 用户反馈实体类
 * 数据库列名使用连字符格式，MyBatis自动转换
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("feedback")
public class Feedback {
    private Long id;
    private Long userId;         // 数据库列：user-id
    private String content;
    private String replyContent;  // 数据库列：reply-content
    private Integer status;
    private LocalDateTime createTime;  // 数据库列：create-time
}
