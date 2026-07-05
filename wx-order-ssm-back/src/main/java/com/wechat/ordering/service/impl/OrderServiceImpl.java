package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Order;
import com.wechat.ordering.entity.OrderDetail;
import com.wechat.ordering.entity.Payment;
import com.wechat.ordering.mapper.OrderDetailMapper;
import com.wechat.ordering.mapper.OrderMapper;
import com.wechat.ordering.mapper.PaymentMapper;
import com.wechat.ordering.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public Map<String, Object> list(Integer page, Integer pageSize, Integer orderStatus) {
        List<Order> all;
        if (orderStatus != null) {
            all = orderMapper.selectByStatus(orderStatus);
        } else {
            all = orderMapper.selectAll();
        }

        int total = all.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Order> pageList = all.subList(Math.min(fromIndex, total), toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", pageList);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Map<String, Object> detail(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        List<OrderDetail> details = orderDetailMapper.selectByOrderId(id);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("details", details);
        return result;
    }

    @Override
    public void updateStatus(Long id, Integer orderStatus) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setOrderStatus(orderStatus);
        // 已支付(1)：记录支付流水
        if (orderStatus == 1 && paymentMapper.selectByOrderId(id) == null) {
            Payment payment = Payment.builder()
                .orderId(id)
                .payNo("PAY" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")))
                .payAmount(order.getPayAmount())
                .payMethod("微信支付")
                .payTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();
            paymentMapper.insert(payment);
        }
        // 已完成(2)：标记已支付
        if (orderStatus == 2) {
            order.setPayStatus(1);
        }
        orderMapper.update(order);
    }
}
