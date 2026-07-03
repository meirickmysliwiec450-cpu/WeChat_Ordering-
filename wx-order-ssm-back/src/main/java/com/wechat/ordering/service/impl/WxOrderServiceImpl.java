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
    public Map<String, Object> submit(Long userId, Long addressId, Map<String, Object> params) {
        // 1. 从前端参数解析基础字段
        String remark = (String) params.get("remark");
        String diningType = (String) params.get("diningType");
        String tableInfo = (String) params.get("tableInfo");
        BigDecimal totalAmount = new BigDecimal(params.get("totalPrice").toString());
        List<Map<String, Object>> itemList = (List<Map<String, Object>>) params.get("items");

        if (itemList == null || itemList.isEmpty()) {
            throw new RuntimeException("购物车为空，无法下单");
        }

        Address addr = null;
        // 只有外送才查询收货地址
        if ("takeout".equals(diningType) && addressId != null) {
            addr = addressMapper.selectById(addressId);
            if (addr == null || !addr.getUserId().equals(userId)) {
                throw new RuntimeException("收货地址不存在");
            }
        }

        // 2. 校验商品状态、构建明细
        List<Map<String, Object>> validItems = new ArrayList<>();
        for (Map<String, Object> item : itemList) {
            Long dishId = Long.valueOf(item.get("dishId").toString());
            Dish dish = dishMapper.selectById(dishId);
            // 菜品上架状态1才允许下单
            if (dish != null && dish.getStatus() != null && dish.getStatus() == 1) {
                validItems.add(item);
            }
        }
        if (validItems.isEmpty()) {
            throw new RuntimeException("没有可下单的菜品");
        }

        // 3. 生成订单号
        String orderNo = "WX" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000));

        // 4. 构建订单实体（区分堂食/外送赋值收货信息）
        Order.OrderBuilder orderBuilder = Order.builder()
                .orderNo(orderNo)
                .userId(userId)
                .totalAmount(totalAmount)
                .payAmount(totalAmount)
                .payStatus(0)         // 未支付
                .orderStatus(3)       // 待支付（状态值已重构：3=待支付 1=已支付 2=已完成 0=已取消）
                .remark(remark)
                .addressId(addressId) // 堂食为null，外送为真实ID
                .createTime(LocalDateTime.now());

        // 外送填充收货人、电话；堂食留空
        if ("takeout".equals(diningType) && addr != null) {
            orderBuilder.receiver(addr.getReceiver())
                    .receiverPhone(addr.getPhone());
        }

        Order order = orderBuilder.build();
        orderMapper.insert(order);
        Order savedOrder = orderMapper.selectByOrderNo(orderNo);

        // 5. 插入订单明细、更新菜品库存销量
        for (Map<String, Object> item : validItems) {
            Long dishId = Long.valueOf(item.get("dishId").toString());
            Integer count = Integer.valueOf(item.get("count").toString());
            BigDecimal price = new BigDecimal(item.get("price").toString());
            BigDecimal amount = new BigDecimal(item.get("amount").toString());

            Dish dish = dishMapper.selectById(dishId);
            OrderDetail detail = OrderDetail.builder()
                    .orderId(savedOrder.getId())
                    .dishId(dishId)
                    .dishName(dish.getDishName())
                    .price(price)
                    .quantity(count)
                    .amount(amount)
                    .createTime(LocalDateTime.now())
                    .build();
            orderDetailMapper.insert(detail);

            // 更新销量、扣库存
            dish.setSales((dish.getSales() != null ? dish.getSales() : 0) + count);
            dish.setStock(dish.getStock() != null ? dish.getStock() - count : 0);
            dishMapper.update(dish);
        }

        // 6. 清空当前用户购物车（和原有逻辑一致）
        List<Cart> userCarts = cartMapper.selectByUserId(userId);
        for (Cart c : userCarts) {
            cartMapper.deleteById(c.getId());
        }

        // 返回订单信息
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
        if (order.getOrderStatus() != 3) throw new RuntimeException("只有待支付状态的订单才能取消");
        order.setOrderStatus(0); // 已取消
        orderMapper.update(order);
    }

    public void updateOrder(Long userId, Long orderId, Map<String, Object> params) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        // 1、修改备注（原有逻辑保留）
        String remark = (String) params.get("remark");
        if (remark != null) {
            order.setRemark(remark);
        }

        // 2、处理状态修改
        Object targetStatusObj = params.get("orderStatus");
        if (targetStatusObj != null) {
            Integer targetStatus = Integer.valueOf(targetStatusObj.toString());
            Integer currentStatus = order.getOrderStatus();

            // 状态流转校验，禁止非法变更
            boolean allowChange = false;
            switch (currentStatus) {
                case 3: // 待支付：可改为 已支付1 / 已取消0
                    if (targetStatus == 1 || targetStatus == 0) allowChange = true;
                    break;
                case 1: // 已支付：只能改为已完成2/已取消0
                    if (targetStatus == 2|| targetStatus == 0) allowChange = true;
                    break;
                case 2: // 已完成 不可修改
                case 0: // 已取消 不可修改
                    allowChange = false;
                    break;
            }
            if (!allowChange) {
                throw new RuntimeException("当前订单状态不支持修改为目标状态");
            }
            // 校验通过才赋值
            order.setOrderStatus(targetStatus);
        }

        orderMapper.update(order);
    }

}
