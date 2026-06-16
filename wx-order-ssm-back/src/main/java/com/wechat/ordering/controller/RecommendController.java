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

    /** AI个性化菜品推荐 */
    @GetMapping
    public Result<Map<String, Object>> recommend(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(recommendService.recommend(userId));
    }
}
