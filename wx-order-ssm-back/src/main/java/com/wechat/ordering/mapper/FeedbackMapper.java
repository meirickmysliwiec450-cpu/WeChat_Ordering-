package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.Feedback;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface FeedbackMapper {
    List<Feedback> selectAll();
    Feedback selectById(Long id);
    List<Feedback> selectByStatus(@Param("status") Integer status);
    int insert(Feedback feedback);
    int update(Feedback feedback);
    int deleteById(Long id);
}
