package com.wechat.ordering.service;

import com.wechat.ordering.entity.OrderComment;
import java.util.Map;

public interface OrderCommentService {
    /** 分页查询评价列表 */
    Map<String, Object> list(Integer page, Integer pageSize, Long orderId, String startDate, String endDate);
    /** 根据ID获取评价详情 */
    OrderComment getById(Long id);
    /** 删除评价 */
    void delete(Long id);
}
