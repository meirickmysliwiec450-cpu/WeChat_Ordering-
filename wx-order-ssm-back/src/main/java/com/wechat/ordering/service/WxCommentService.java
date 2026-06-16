package com.wechat.ordering.service;

import com.wechat.ordering.entity.OrderComment;
import java.util.List;

public interface WxCommentService {
    /** 对已完成订单进行评价 */
    void submit(Long userId, Long orderId, Integer score, String content, String photo);
    /** 查看订单评价 */
    List<OrderComment> getByOrderId(Long orderId);
}
