package com.wechat.ordering.service;

import com.wechat.ordering.entity.OrderComment;
import java.util.List;

public interface WxCommentService {
    void submit(Long userId, Long orderId, Integer score, String content, String photo);
    List<OrderComment> getByOrderId(Long orderId);
    /** 查看用户的所有评价 */
    List<OrderComment> getByUserId(Long userId);
}
