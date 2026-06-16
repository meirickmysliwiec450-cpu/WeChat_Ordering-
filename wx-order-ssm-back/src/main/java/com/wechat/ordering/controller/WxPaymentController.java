package com.wechat.ordering.controller;

import com.wechat.ordering.service.WxPaymentService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/wx/payments")
public class WxPaymentController {

    @Autowired
    private WxPaymentService wxPaymentService;

    /** 发起支付（模拟） */
    @PostMapping
    public Result<Map<String, Object>> pay(HttpServletRequest request, @RequestBody Map<String, Object> params) {
        Long userId = (Long) request.getAttribute("userId");
        Long orderId = params.get("orderId") != null ? ((Number) params.get("orderId")).longValue() : null;
        String payMethod = (String) params.get("payMethod");
        if (orderId == null) return Result.error("orderId不能为空");
        if (payMethod == null) payMethod = "wechat";
        try {
            return Result.success(wxPaymentService.pay(userId, orderId, payMethod));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
