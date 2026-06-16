package com.wechat.ordering.service;

import java.util.Map;

public interface RecommendService {
    /** AI个性化推荐：根据用户历史订单和当前时段推荐菜品 */
    Map<String, Object> recommend(Long userId);
}
