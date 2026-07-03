package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Order;
import com.wechat.ordering.entity.OrderComment;
import com.wechat.ordering.mapper.OrderCommentMapper;
import com.wechat.ordering.mapper.OrderMapper;
import com.wechat.ordering.service.WxCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WxCommentServiceImpl implements WxCommentService {

    @Autowired private OrderCommentMapper orderCommentMapper;
    @Autowired private OrderMapper orderMapper;

    @Override
    public void submit(Long userId, Long orderId, Integer score, String content, String photo) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new RuntimeException("订单不存在");
        if (order.getOrderStatus() != 2) throw new RuntimeException("只有已完成的订单才能评价");

        // 检查是否已评价
        List<OrderComment> existing = orderCommentMapper.selectByOrderId(orderId);
        if (!existing.isEmpty()) throw new RuntimeException("该订单已评价过");

        OrderComment comment = OrderComment.builder()
                .orderId(orderId).userId(userId).score(score)
                .content(content).photo(photo)
                .createTime(LocalDateTime.now()).build();
        orderCommentMapper.insert(comment);
    }

    @Override
    public List<OrderComment> getByOrderId(Long orderId) {
        return orderCommentMapper.selectByOrderId(orderId);
    }
}
