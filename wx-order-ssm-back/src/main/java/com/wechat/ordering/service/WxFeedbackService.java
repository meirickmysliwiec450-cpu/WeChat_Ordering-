package com.wechat.ordering.service;

import com.wechat.ordering.entity.Feedback;
import java.util.List;

public interface WxFeedbackService {
    /** 提交反馈 */
    void submit(Long userId, String content);
    /** 查看我的反馈列表 */
    List<Feedback> list(Long userId);
}
