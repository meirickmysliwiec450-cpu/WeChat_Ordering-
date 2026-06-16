package com.wechat.ordering.service;

import com.wechat.ordering.entity.Order;
import java.util.Map;

public interface WxOrderService {
    /** 提交订单（从购物车生成，含订单明细、清空购物车） */
    Map<String, Object> submit(Long userId, Long addressId, String remark);
    /** 用户订单列表（支持按状态筛选） */
    Map<String, Object> list(Long userId, Integer page, Integer pageSize, Integer status);
    /** 订单详情（含菜品明细） */
    Map<String, Object> detail(Long userId, Long orderId);
    /** 取消订单 */
    void cancel(Long userId, Long orderId);
}
