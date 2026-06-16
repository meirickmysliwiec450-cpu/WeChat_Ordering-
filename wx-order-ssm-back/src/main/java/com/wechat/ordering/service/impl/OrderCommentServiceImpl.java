package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.OrderComment;
import com.wechat.ordering.mapper.OrderCommentMapper;
import com.wechat.ordering.service.OrderCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderCommentServiceImpl implements OrderCommentService {

    @Autowired
    private OrderCommentMapper orderCommentMapper;

    @Override
    public Map<String, Object> list(Integer page, Integer pageSize, Long orderId) {
        List<OrderComment> all;
        if (orderId != null) {
            all = orderCommentMapper.selectByOrderId(orderId);
        } else {
            all = orderCommentMapper.selectAll();
        }

        int total = all.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<OrderComment> pageList = all.subList(Math.min(fromIndex, total), toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", pageList);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public OrderComment getById(Long id) {
        return orderCommentMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        OrderComment comment = orderCommentMapper.selectById(id);
        if (comment == null) {
            throw new RuntimeException("评价不存在");
        }
        orderCommentMapper.deleteById(id);
    }
}
