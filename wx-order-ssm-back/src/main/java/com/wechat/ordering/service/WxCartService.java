package com.wechat.ordering.service;

import com.wechat.ordering.entity.Cart;
import java.util.List;
import java.util.Map;

public interface WxCartService {
    /** 获取用户购物车列表（含菜品详情） */
    List<Map<String, Object>> list(Long userId);
    /** 添加菜品到购物车 */
    void add(Long userId, Long dishId, Integer quantity);
    /** 修改购物车数量 */
    void updateQuantity(Long userId, Long cartId, Integer quantity);
    /** 删除购物车项 */
    void delete(Long userId, Long cartId);
    /** 清空购物车 */
    void clear(Long userId);
}
