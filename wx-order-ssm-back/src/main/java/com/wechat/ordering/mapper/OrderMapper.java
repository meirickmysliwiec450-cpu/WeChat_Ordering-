package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.Order;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OrderMapper {
    List<Order> selectAll();
    Order selectById(Long id);
    List<Order> selectByUserId(@Param("userId") Long userId);
    List<Order> selectByStatus(@Param("orderStatus") Integer orderStatus);
    Order selectByOrderNo(@Param("orderNo") String orderNo);
    int insert(Order order);
    int update(Order order);
    int deleteById(Long id);
}
