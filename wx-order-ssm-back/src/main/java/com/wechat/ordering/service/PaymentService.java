package com.wechat.ordering.service;

import com.wechat.ordering.entity.Payment;
import java.util.Map;

public interface PaymentService {
    Map<String, Object> list(Integer page, Integer pageSize, String startDate, String endDate);
    Payment getById(Long id);
}
