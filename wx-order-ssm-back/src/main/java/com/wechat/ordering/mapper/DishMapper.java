package com.wechat.ordering.mapper;

import com.wechat.ordering.entity.Dish;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface DishMapper {
    List<Dish> selectAll();
    Dish selectById(Long id);
    List<Dish> selectByCategoryId(@Param("categoryId") Long categoryId);
    List<Dish> selectByStatus(@Param("status") Integer status);
    int insert(Dish dish);
    int update(Dish dish);
    int deleteById(Long id);
}
