package com.wechat.ordering.controller;

import com.wechat.ordering.service.RecommendService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/wx/recommend")
public class RecommendController {

    @Autowired
    private RecommendService recommendService;

    /** AI个性化菜品推荐（基于历史+时段） */
    @GetMapping
    public Result<Map<String, Object>> recommend(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(recommendService.recommend(userId));
    }

    /** AI对话推荐（基于用户文字偏好） */
    @PostMapping("/chat")
    public Result<Map<String, Object>> recommendByChat(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        String message = params.get("message");
        if (message == null || message.isEmpty()) return Result.error("请输入你的口味偏好");
        return Result.success(recommendService.recommendByChat(userId, message));
    }
}
