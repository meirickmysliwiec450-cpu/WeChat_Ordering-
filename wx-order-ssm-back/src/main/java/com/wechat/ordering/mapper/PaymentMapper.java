package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.Payment;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PaymentMapper {
    List<Payment> selectAll();
    Payment selectById(Long id);
    Payment selectByOrderId(@Param("orderId") Long orderId);
    List<Payment> selectByUserId(@Param("userId") Long userId);
    List<Payment> selectByDateRange(@Param("startDate") String startDate, @Param("endDate") String endDate);
    int insert(Payment payment);
    int update(Payment payment);
    int deleteById(Long id);
}
