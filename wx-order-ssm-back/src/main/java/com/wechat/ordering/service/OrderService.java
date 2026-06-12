package com.wechat.ordering.service;

import com.wechat.ordering.entity.Order;
import com.wechat.ordering.entity.OrderDetail;
import java.util.List;
import java.util.Map;

public interface OrderService {
    /** 分页查询订单列表，支持按状态筛选 */
    Map<String, Object> list(Integer page, Integer pageSize, Integer orderStatus);
    /** 查询订单详情（含订单明细） */
    Map<String, Object> detail(Long id);
    /** 更新订单状态 */
    void updateStatus(Long id, Integer orderStatus);
}
