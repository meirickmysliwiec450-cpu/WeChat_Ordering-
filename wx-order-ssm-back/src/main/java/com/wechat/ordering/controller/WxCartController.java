package com.wechat.ordering.controller;

import com.wechat.ordering.service.WxCartService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wx/cart")
public class WxCartController {

    @Autowired
    private WxCartService wxCartService;

    /** 获取我的购物车 */
    @GetMapping
    public Result<List<Map<String, Object>>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(wxCartService.list(userId));
    }

    /** 添加菜品到购物车 */
    @PostMapping
    public Result<?> add(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long dishId = params.get("dishId") != null ? ((Number) params.get("dishId")).longValue() : null;
        Integer quantity = params.get("quantity") != null ? (Integer) params.get("quantity") : 1;
        if (dishId == null) return Result.error("dishId不能为空");
        try {
            wxCartService.add(userId, dishId, quantity);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 修改购物车数量 */
    @PutMapping("/{id}")
    public Result<?> update(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Integer> params) {
        Long userId = (Long) request.getAttribute("userId");
        Integer quantity = params.get("quantity");
        if (quantity == null) return Result.error("quantity不能为空");
        try {
            wxCartService.updateQuantity(userId, id, quantity);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 删除购物车项 */
    @DeleteMapping("/{id}")
    public Result<?> delete(HttpServletRequest request, @PathVariable Long id) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            wxCartService.delete(userId, id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 清空购物车 */
    @DeleteMapping("/clear")
    public Result<?> clear(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        wxCartService.clear(userId);
        return Result.success();
    }
}
