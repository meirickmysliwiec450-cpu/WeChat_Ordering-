package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.Cart;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CartMapper {
    List<Cart> selectAll();
    List<Cart> selectByUserId(@Param("userId") Long userId);
    Cart selectById(Long id);
    int insert(Cart cart);
    int update(Cart cart);
    int deleteById(Long id);
}
