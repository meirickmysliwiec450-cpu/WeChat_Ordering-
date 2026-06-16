package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Category;
import com.wechat.ordering.entity.Dish;
import com.wechat.ordering.mapper.CategoryMapper;
import com.wechat.ordering.mapper.DishMapper;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/wx/categories")
public class WxCategoryController {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private DishMapper dishMapper;

    /** 获取所有分类及分类下的菜品（仅上架菜品） */
    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        List<Category> categories = categoryMapper.selectAll();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Category cat : categories) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", cat.getId());
            item.put("categoryName", cat.getCategoryName());
            item.put("sort", cat.getSort());
            List<Dish> dishes = dishMapper.selectByCategoryId(cat.getId());
            dishes.removeIf(d -> d.getStatus() == null || d.getStatus() != 1);
            item.put("dishes", dishes);
            result.add(item);
        }
        result.sort(Comparator.comparing(m -> (Integer) m.get("sort")));
        return Result.success(result);
    }
}
