package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Dish;
import com.wechat.ordering.mapper.DishMapper;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/wx/dishes")
public class WxDishController {

    @Autowired
    private DishMapper dishMapper;

    /** 按分类获取菜品（仅返回上架的） */
    @GetMapping
    public Result<List<Dish>> byCategory(@RequestParam(required = false) Long categoryId) {
        List<Dish> dishes = (categoryId != null) ? dishMapper.selectByCategoryId(categoryId) : dishMapper.selectAll();
        dishes = dishes.stream().filter(d -> d.getStatus() != null && d.getStatus() == 1).collect(Collectors.toList());
        return Result.success(dishes);
    }

    /** 搜索菜品 */
    @GetMapping("/search")
    public Result<List<Dish>> search(@RequestParam String keyword) {
        List<Dish> result = dishMapper.selectAll().stream()
                .filter(d -> d.getStatus() != null && d.getStatus() == 1)
                .filter(d -> d.getDishName() != null && d.getDishName().contains(keyword))
                .collect(Collectors.toList());
        return Result.success(result);
    }

    /** 获取菜品详情 */
    @GetMapping("/{id}")
    public Result<Dish> getById(@PathVariable Long id) {
        Dish dish = dishMapper.selectById(id);
        if (dish == null) return Result.error("菜品不存在");
        return Result.success(dish);
    }
}
