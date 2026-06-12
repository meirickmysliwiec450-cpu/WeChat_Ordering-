package com.wechat.ordering.service;

import com.wechat.ordering.entity.Dish;
import java.util.Map;

public interface DishService {
    /** 分页查询菜品列表，支持按分类筛选 */
    Map<String, Object> list(Integer page, Integer pageSize, Long categoryId, String keyword);
    Dish getById(Long id);
    void add(Dish dish);
    void update(Dish dish);
    void updateStatus(Long id, Integer status);
    void delete(Long id);
}
