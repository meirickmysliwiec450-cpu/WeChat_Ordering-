package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.OrderComment;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OrderCommentMapper {
    List<OrderComment> selectAll();
    OrderComment selectById(Long id);
    List<OrderComment> selectByOrderId(@Param("orderId") Long orderId);
    int insert(OrderComment orderComment);
    int update(OrderComment orderComment);
    int deleteById(Long id);
}
