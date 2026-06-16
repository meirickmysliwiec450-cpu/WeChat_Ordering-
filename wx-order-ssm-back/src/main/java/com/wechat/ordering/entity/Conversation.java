package com.wechat.ordering.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.type.Alias;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Alias("conversation")
public class Conversation {
    private Long id;
    private Long userId;
    private String role;       // user / assistant
    private String content;
    private LocalDateTime createTime;
}
