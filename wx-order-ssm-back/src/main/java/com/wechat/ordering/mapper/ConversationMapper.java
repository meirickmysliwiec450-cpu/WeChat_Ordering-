package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.Conversation;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ConversationMapper {
    List<Conversation> selectByUserId(@Param("userId") Long userId);
    int insert(Conversation conversation);
    /** 清除用户对话历史 */
    int deleteByUserId(@Param("userId") Long userId);
}
