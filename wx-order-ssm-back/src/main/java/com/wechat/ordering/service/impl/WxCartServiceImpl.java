package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Cart;
import com.wechat.ordering.entity.Dish;
import com.wechat.ordering.mapper.CartMapper;
import com.wechat.ordering.mapper.DishMapper;
import com.wechat.ordering.service.WxCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class WxCartServiceImpl implements WxCartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private DishMapper dishMapper;

    @Override
    public List<Map<String, Object>> list(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Cart cart : carts) {
            Dish dish = dishMapper.selectById(cart.getDishId());
            Map<String, Object> item = new HashMap<>();
            item.put("id", cart.getId());
            item.put("dishId", cart.getDishId());
            item.put("quantity", cart.getQuantity());
            item.put("totalPrice", cart.getTotalPrice());
            if (dish != null) {
                item.put("dishName", dish.getDishName());
                item.put("image", dish.getImage());
                item.put("price", dish.getPrice());
                item.put("stock", dish.getStock());
            }
            result.add(item);
        }
        return result;
    }

    @Override
    public void add(Long userId, Long dishId, Integer quantity) {
        Dish dish = dishMapper.selectById(dishId);
        if (dish == null) throw new RuntimeException("菜品不存在");
        if (dish.getStock() != null && quantity > dish.getStock()) throw new RuntimeException("库存不足");

        // 检查购物车中是否已有该菜品，有则累加数量
        List<Cart> existing = cartMapper.selectByUserId(userId);
        for (Cart c : existing) {
            if (c.getDishId().equals(dishId)) {
                c.setQuantity(c.getQuantity() + quantity);
                c.setTotalPrice(dish.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())));
                cartMapper.update(c);
                return;
            }
        }

        Cart cart = Cart.builder()
                .userId(userId).dishId(dishId).quantity(quantity)
                .totalPrice(dish.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .createTime(LocalDateTime.now()).build();
        cartMapper.insert(cart);
    }

    @Override
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) throw new RuntimeException("购物车项不存在");
        if (quantity <= 0) {
            cartMapper.deleteById(cartId);
            return;
        }
        Dish dish = dishMapper.selectById(cart.getDishId());
        cart.setQuantity(quantity);
        cart.setTotalPrice(dish.getPrice().multiply(BigDecimal.valueOf(quantity)));
        cartMapper.update(cart);
    }

    @Override
    public void delete(Long userId, Long cartId) {
        Cart cart = cartMapper.selectById(cartId);
        if (cart == null || !cart.getUserId().equals(userId)) throw new RuntimeException("购物车项不存在");
        cartMapper.deleteById(cartId);
    }

    @Override
    public void clear(Long userId) {
        List<Cart> carts = cartMapper.selectByUserId(userId);
        for (Cart c : carts) cartMapper.deleteById(c.getId());
    }
}
