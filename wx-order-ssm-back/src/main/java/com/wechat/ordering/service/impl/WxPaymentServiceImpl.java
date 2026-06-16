package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Order;
import com.wechat.ordering.entity.Payment;
import com.wechat.ordering.mapper.OrderMapper;
import com.wechat.ordering.mapper.PaymentMapper;
import com.wechat.ordering.service.WxPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class WxPaymentServiceImpl implements WxPaymentService {

    @Autowired private PaymentMapper paymentMapper;
    @Autowired private OrderMapper orderMapper;

    @Override
    public Map<String, Object> pay(Long userId, Long orderId, String payMethod) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new RuntimeException("订单不存在");
        if (order.getPayStatus() != null && order.getPayStatus() == 1) throw new RuntimeException("订单已支付");

        // 检查是否已有支付记录
        Payment existPay = paymentMapper.selectByOrderId(orderId);
        if (existPay != null) throw new RuntimeException("已有支付记录，请勿重复支付");

        // 生成支付流水号（模拟）
        String payNo = "PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));

        Payment payment = Payment.builder()
                .orderId(orderId).payNo(payNo).payAmount(order.getPayAmount())
                .payMethod(payMethod).payTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();
        paymentMapper.insert(payment);

        // 更新订单支付状态
        order.setPayStatus(1);
        orderMapper.update(order);

        Map<String, Object> result = new HashMap<>();
        result.put("payNo", payNo);
        result.put("payAmount", order.getPayAmount());
        return result;
    }
}
