package com.wechat.ordering.controller;

import com.wechat.ordering.service.NutritionService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/wx/nutrition")
public class NutritionController {

    @Autowired
    private NutritionService nutritionService;

    /** AI膳食营养多轮对话 */
    @PostMapping("/chat")
    public Result<Map<String, Object>> chat(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        String message = params.get("message");
        if (message == null || message.isEmpty()) return Result.error("消息不能为空");
        return Result.success(nutritionService.chat(userId, message));
    }

    /** 生成个人饮食习惯周报 */
    @GetMapping("/report")
    public Result<Map<String, Object>> report(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(nutritionService.weeklyReport(userId));
    }
}
