package com.wechat.ordering.service;

import java.util.Map;

public interface NutritionService {
    /** 多轮对话：根据用户下单菜品进行营养分析对话 */
    Map<String, Object> chat(Long userId, String message);
    /** 生成个人饮食习惯周报 */
    Map<String, Object> weeklyReport(Long userId);
}
