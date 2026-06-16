package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Dish;
import com.wechat.ordering.mapper.DishMapper;
import com.wechat.ordering.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> list(Integer page, Integer pageSize, Long categoryId, String keyword) {
        List<Dish> all;

        if (categoryId != null) {
            all = dishMapper.selectByCategoryId(categoryId);
        } else {
            all = dishMapper.selectAll();
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim().toLowerCase();
            all = all.stream()
                    .filter(d -> d.getDishName() != null && d.getDishName().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }

        int total = all.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Dish> pageList = all.subList(Math.min(fromIndex, total), toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", pageList);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Dish getById(Long id) {
        return dishMapper.selectById(id);
    }

    @Override
    public void add(Dish dish) {
        dish.setCreateTime(LocalDateTime.now());
        if (dish.getSales() == null) dish.setSales(0);
        if (dish.getStatus() == null) dish.setStatus(1);
        // 手动指定ID = 当前最大ID + 1，彻底绕过AUTO_INCREMENT跳号问题
        Long maxId = jdbcTemplate.queryForObject(
            "SELECT COALESCE(MAX(id), 0) FROM t_dish", Long.class);
        dish.setId((maxId != null ? maxId : 0) + 1);
        dishMapper.insertWithId(dish);
    }

    @Override
    public void update(Dish dish) {
        dishMapper.update(dish);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }
        dish.setStatus(status);
        dishMapper.update(dish);
    }

    @Override
    public void delete(Long id) {
        dishMapper.deleteById(id);
    }
}
