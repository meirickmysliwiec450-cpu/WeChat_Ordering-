package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Feedback;
import com.wechat.ordering.mapper.FeedbackMapper;
import com.wechat.ordering.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public Map<String, Object> list(Integer page, Integer pageSize, Integer status) {
        List<Feedback> all;
        if (status != null) {
            all = feedbackMapper.selectByStatus(status);
        } else {
            all = feedbackMapper.selectAll();
        }

        int total = all.size();
        int fromIndex = (page - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<Feedback> pageList = all.subList(Math.min(fromIndex, total), toIndex);

        Map<String, Object> result = new HashMap<>();
        result.put("total", total);
        result.put("list", pageList);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    @Override
    public Feedback getById(Long id) {
        return feedbackMapper.selectById(id);
    }

    @Override
    public void reply(Long id, String replyContent) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }
        feedback.setReplyContent(replyContent);
        feedback.setStatus(1); // 已回复
        feedbackMapper.update(feedback);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Feedback feedback = feedbackMapper.selectById(id);
        if (feedback == null) {
            throw new RuntimeException("反馈不存在");
        }
        feedback.setStatus(status);
        feedbackMapper.update(feedback);
    }
}
