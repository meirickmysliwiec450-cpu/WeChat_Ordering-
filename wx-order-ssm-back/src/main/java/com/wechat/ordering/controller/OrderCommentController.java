package com.wechat.ordering.controller;

import com.wechat.ordering.entity.OrderComment;
import com.wechat.ordering.service.OrderCommentService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/comments")
public class OrderCommentController {

    @Autowired
    private OrderCommentService orderCommentService;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(orderCommentService.list(page, pageSize, orderId, startDate, endDate));
    }

    @GetMapping("/{id}")
    public Result<OrderComment> getById(@PathVariable Long id) {
        OrderComment comment = orderCommentService.getById(id);
        if (comment == null) {
            return Result.error("评价不存在");
        }
        return Result.success(comment);
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        try {
            orderCommentService.delete(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
