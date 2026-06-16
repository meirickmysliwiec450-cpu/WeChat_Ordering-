package com.wechat.ordering.service;

import com.wechat.ordering.entity.Payment;
import java.util.Map;

public interface PaymentService {
    /** 分页查询支付记录列表 */
    Map<String, Object> list(Integer page, Integer pageSize, Long orderId);
    /** 根据ID获取支付记录详情 */
    Payment getById(Long id);
}
