package com.wechat.ordering.controller;

import com.wechat.ordering.entity.OrderComment;
import com.wechat.ordering.service.WxCommentService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wx/comments")
public class WxCommentController {

    @Autowired
    private WxCommentService wxCommentService;

    /** 对已完成订单进行评价 */
    @PostMapping
    public Result<?> submit(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long orderId = params.get("orderId") != null ? ((Number) params.get("orderId")).longValue() : null;
        Integer score = params.get("score") != null ? (Integer) params.get("score") : null;
        String content = (String) params.get("content");
        String photo = (String) params.get("photo");
        if (orderId == null) return Result.error("orderId不能为空");
        if (score == null) return Result.error("评分不能为空");
        try {
            wxCommentService.submit(userId, orderId, score, content, photo);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 查看某订单的评价 */
    @GetMapping("/order/{orderId}")
    public Result<List<OrderComment>> getByOrderId(@PathVariable Long orderId) {
        return Result.success(wxCommentService.getByOrderId(orderId));
    }
}
