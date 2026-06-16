package com.wechat.ordering.service;

import java.util.Map;

public interface WxPaymentService {
    /** 发起支付（模拟），返回支付流水号和金额 */
    Map<String, Object> pay(Long userId, Long orderId, String payMethod);
}
