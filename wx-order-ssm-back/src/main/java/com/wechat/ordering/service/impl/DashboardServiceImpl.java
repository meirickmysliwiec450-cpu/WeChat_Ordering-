package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Order;
import com.wechat.ordering.mapper.*;
import com.wechat.ordering.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public Map<String, Object> getStats() {
        int totalUsers = userMapper.selectAll().size();
        List<Order> allOrders = orderMapper.selectAll();
        int totalOrders = allOrders.size();
        int totalDishes = dishMapper.selectAll().size();
        int pendingFeedbacks = feedbackMapper.selectByStatus(0).size();

        // 计算总营业额
        BigDecimal totalRevenue = allOrders.stream()
                .map(o -> o.getPayAmount() != null ? o.getPayAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 今日订单数
        LocalDate today = LocalDate.now();
        long todayOrders = allOrders.stream()
                .filter(o -> o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(today))
                .count();

        // 各状态订单数
        long pendingOrders = allOrders.stream().filter(o -> o.getOrderStatus() == 1).count();
        long acceptedOrders = allOrders.stream().filter(o -> o.getOrderStatus() == 2).count();
        long completedOrders = allOrders.stream().filter(o -> o.getOrderStatus() == 3).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalOrders", totalOrders);
        stats.put("totalDishes", totalDishes);
        stats.put("totalRevenue", totalRevenue);
        stats.put("todayOrders", todayOrders);
        stats.put("pendingFeedbacks", pendingFeedbacks);
        stats.put("pendingOrders", pendingOrders);
        stats.put("acceptedOrders", acceptedOrders);
        stats.put("completedOrders", completedOrders);
        return stats;
    }
}
