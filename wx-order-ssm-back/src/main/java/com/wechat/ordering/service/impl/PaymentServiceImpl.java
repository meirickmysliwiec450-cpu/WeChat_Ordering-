package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Payment;
import com.wechat.ordering.mapper.PaymentMapper;
import com.wechat.ordering.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public Map<String, Object> list(Integer page, Integer pageSize, Long orderId) {
        List<Payment> all;
        if (orderId != null) {
            Payment payment = paymentMapper.selectByOrderId(orderId);
            all = payment != null ? List.of(payment) : List.of();
        } else {
            all = paymentMapper.selectAll();
        }

        int total = all.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Payment> pageList = all.subList(Math.min(fromIndex, total), toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", pageList);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Payment getById(Long id) {
        return paymentMapper.selectById(id);
    }
}
