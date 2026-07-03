package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Payment;
import com.wechat.ordering.service.PaymentService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(paymentService.list(page, pageSize, startDate, endDate));
    }

    @GetMapping("/{id}")
    public Result<Payment> getById(@PathVariable Long id) {
        Payment payment = paymentService.getById(id);
        if (payment == null) {
            return Result.error("支付记录不存在");
        }
        return Result.success(payment);
    }
}
