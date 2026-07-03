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

        // 处理前端字符串格式的 id
        Object idObj = params.get("id");
        Long id = null;
        try {
            if (idObj != null) {
                String idStr = idObj.toString().trim();
                if (!idStr.isEmpty()) {
                    id = Long.parseLong(idStr);
                }
            }
        } catch (NumberFormatException e) {
            return Result.error("订单id必须是合法数字");
        }

        if (id == null) return Result.error("id不能为空");

        // 兼容字符串/数字类型的score
        Object scoreObj = params.get("score");
        Integer score = null;
        try {
            if (scoreObj != null) {
                String scoreStr = scoreObj.toString().trim();
                if (!scoreStr.isEmpty()) {
                    score = Integer.parseInt(scoreStr);
                }
            }
        } catch (NumberFormatException e) {
            return Result.error("评分必须是数字");
        }
        if (score == null) return Result.error("评分不能为空");

        String content = params.get("content") == null ? "" : params.get("content").toString();
        String photo = params.get("photo") == null ? "" : params.get("photo").toString();

        try {
            wxCommentService.submit(userId, id, score, content, photo);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /** 查看某订单的评价 */
    @GetMapping("/order/{id}")
    public Result<List<OrderComment>> getByOrderId(@PathVariable Long id) {
        return Result.success(wxCommentService.getByOrderId(id));
    }
}
