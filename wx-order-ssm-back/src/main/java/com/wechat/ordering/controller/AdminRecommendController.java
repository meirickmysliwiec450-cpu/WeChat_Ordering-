package com.wechat.ordering.controller;

import com.wechat.ordering.service.RecommendService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/recommend")
public class AdminRecommendController {

    @Autowired
    private RecommendService recommendService;

    /** 管理员预览AI推荐效果（模拟一个老用户） */
    @GetMapping("/preview")
    public Result<Map<String, Object>> preview() {
        // 用ID=1的用户模拟（如果该用户有历史订单则AI推荐更丰富）
        return Result.success(recommendService.recommend(1L));
    }
}
