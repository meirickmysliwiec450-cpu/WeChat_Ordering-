package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Feedback;
import com.wechat.ordering.service.FeedbackService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status) {
        return Result.success(feedbackService.list(page, pageSize, status));
    }

    @GetMapping("/{id}")
    public Result<Feedback> getById(@PathVariable Long id) {
        Feedback feedback = feedbackService.getById(id);
        if (feedback == null) {
            return Result.error("反馈不存在");
        }
        return Result.success(feedback);
    }

    @PostMapping("/{id}/reply")
    public Result<?> reply(@PathVariable Long id, @RequestBody Map<String, String> params) {
        try {
            feedbackService.reply(id, params.get("replyContent"));
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            feedbackService.updateStatus(id, params.get("status"));
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
