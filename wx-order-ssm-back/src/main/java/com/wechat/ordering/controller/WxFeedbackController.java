package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Feedback;
import com.wechat.ordering.service.WxFeedbackService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/wx/feedbacks")
public class WxFeedbackController {

    @Autowired
    private WxFeedbackService wxFeedbackService;

    /** 提交反馈 */
    @PostMapping
    public Result<?> submit(HttpServletRequest request, @RequestBody Map<String, String> params) {
        Long userId = (Long) request.getAttribute("userId");
        String content = params.get("content");
        if (content == null || content.isEmpty()) return Result.error("反馈内容不能为空");
        wxFeedbackService.submit(userId, content);
        return Result.success();
    }

    /** 查看我的反馈列表 */
    @GetMapping
    public Result<List<Feedback>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(wxFeedbackService.list(userId));
    }
}
