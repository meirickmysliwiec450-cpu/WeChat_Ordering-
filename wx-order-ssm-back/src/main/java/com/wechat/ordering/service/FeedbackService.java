package com.wechat.ordering.service;

import com.wechat.ordering.entity.Feedback;
import java.util.Map;

public interface FeedbackService {
    /** 分页查询反馈列表 */
    Map<String, Object> list(Integer page, Integer pageSize, Integer status);
    Feedback getById(Long id);
    /** 管理员回复反馈 */
    void reply(Long id, String replyContent);
    /** 更新处理状态 */
    void updateStatus(Long id, Integer status);
}
