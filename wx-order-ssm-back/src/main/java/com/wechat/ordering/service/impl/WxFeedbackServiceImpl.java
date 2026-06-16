package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Feedback;
import com.wechat.ordering.mapper.FeedbackMapper;
import com.wechat.ordering.service.WxFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WxFeedbackServiceImpl implements WxFeedbackService {

    @Autowired
    private FeedbackMapper feedbackMapper;

    @Override
    public void submit(Long userId, String content) {
        Feedback feedback = Feedback.builder()
                .userId(userId).content(content).status(0)
                .createTime(LocalDateTime.now()).build();
        feedbackMapper.insert(feedback);
    }

    @Override
    public List<Feedback> list(Long userId) {
        return feedbackMapper.selectAll().stream()
                .filter(f -> f.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
