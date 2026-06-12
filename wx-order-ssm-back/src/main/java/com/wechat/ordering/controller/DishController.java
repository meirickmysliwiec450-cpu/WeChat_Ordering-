package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Dish;
import com.wechat.ordering.service.DishService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/dishes")
public class DishController {

    @Autowired
    private DishService dishService;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(dishService.list(page, pageSize, categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<Dish> getById(@PathVariable Long id) {
        Dish dish = dishService.getById(id);
        if (dish == null) {
            return Result.error("菜品不存在");
        }
        return Result.success(dish);
    }

    @PostMapping
    public Result<?> add(@RequestBody Dish dish) {
        dishService.add(dish);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Dish dish) {
        dish.setId(id);
        dishService.update(dish);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            dishService.updateStatus(id, params.get("status"));
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        dishService.delete(id);
        return Result.success();
    }
}
