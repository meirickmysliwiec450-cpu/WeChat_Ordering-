package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.OrderDetail;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface OrderDetailMapper {
    List<OrderDetail> selectAll();
    OrderDetail selectById(Long id);
    List<OrderDetail> selectByOrderId(@Param("orderId") Long orderId);
    int insert(OrderDetail orderDetail);
    int update(OrderDetail orderDetail);
    int deleteById(Long id);
}
