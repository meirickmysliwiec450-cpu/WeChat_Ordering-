package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.*;
import com.wechat.ordering.mapper.*;
import com.wechat.ordering.service.WxOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WxOrderServiceImpl implements WxOrderService {

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderDetailMapper orderDetailMapper;
    @Autowired private CartMapper cartMapper;
    @Autowired private DishMapper dishMapper;
    @Autowired private AddressMapper addressMapper;

    @Override
    @Transactional
    public Map<String, Object> submit(Long userId, Long addressId, String remark) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        if (carts.isEmpty()) throw new RuntimeException("购物车为空，无法下单");

        Address addr = addressMapper.selectById(addressId);
        if (addr == null || !addr.getUserId().equals(userId)) throw new RuntimeException("收货地址不存在");

        // 计算总金额
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<Cart> validCarts = new ArrayList<>();
        for (Cart c : carts) {
            Dish dish = dishMapper.selectById(c.getDishId());
            if (dish != null && dish.getStatus() != null && dish.getStatus() == 1) {
                totalAmount = totalAmount.add(dish.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
                validCarts.add(c);
            }
        }
        if (validCarts.isEmpty()) throw new RuntimeException("没有可下单的菜品");

        // 生成订单号
        String orderNo = "WX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));

        // 创建订单
        Order order = Order.builder()
                .orderNo(orderNo).userId(userId).totalAmount(totalAmount)
                .payAmount(totalAmount).payStatus(0).orderStatus(1) // 待处理
                .remark(remark).addressId(addressId)
                .receiver(addr.getReceiver()).receiverPhone(addr.getPhone())
                .createTime(LocalDateTime.now()).build();
        orderMapper.insert(order);

        // 获取自增ID
        Order savedOrder = orderMapper.selectByOrderNo(orderNo);

        // 创建订单明细 + 更新菜品销量
        for (Cart c : validCarts) {
            Dish dish = dishMapper.selectById(c.getDishId());
            OrderDetail detail = OrderDetail.builder()
                    .orderId(savedOrder.getId()).dishId(c.getDishId())
                    .dishName(dish.getDishName()).price(dish.getPrice())
                    .quantity(c.getQuantity())
                    .amount(dish.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                    .createTime(LocalDateTime.now()).build();
            orderDetailMapper.insert(detail);
            // 更新销量和库存
            dish.setSales((dish.getSales() != null ? dish.getSales() : 0) + c.getQuantity());
            dish.setStock(dish.getStock() != null ? dish.getStock() - c.getQuantity() : 0);
            dishMapper.update(dish);
        }

        // 清空购物车
        for (Cart c : carts) cartMapper.deleteById(c.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", savedOrder.getId());
        result.put("orderNo", savedOrder.getOrderNo());
        result.put("totalAmount", savedOrder.getTotalAmount());
        return result;
    }

    @Override
    public Map<String, Object> list(Long userId, Integer page, Integer pageSize, Integer status) {
        List<Order> all = orderMapper.selectByUserId(userId);
        if (status != null) {
            all.removeIf(o -> !o.getOrderStatus().equals(status));
        }
        all.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime())); // 按时间倒序

        int total = all.size();
        int from = (page - 1) * pageSize;
        int to = Math.min(from + pageSize, total);
        List<Order> pageList = all.subList(Math.min(from, total), to);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", pageList);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Map<String, Object> detail(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new RuntimeException("订单不存在");
        List<OrderDetail> details = orderDetailMapper.selectByOrderId(orderId);

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("details", details);
        return result;
    }

    @Override
    public void cancel(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) throw new RuntimeException("订单不存在");
        if (order.getOrderStatus() != 1) throw new RuntimeException("只有待处理状态的订单才能取消");
        order.setOrderStatus(0); // 已取消
        orderMapper.update(order);
    }
}
