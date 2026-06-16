package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.*;
import com.wechat.ordering.mapper.*;
import com.wechat.ordering.service.NutritionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NutritionServiceImpl implements NutritionService {

    @Value("${ai.api.url}") private String aiApiUrl;
    @Value("${ai.api.key}") private String aiApiKey;
    @Value("${ai.api.model}") private String aiModel;

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderDetailMapper orderDetailMapper;
    @Autowired private DishMapper dishMapper;
    @Autowired private ConversationMapper conversationMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> chat(Long userId, String message) {
        // 1. 获取用户今日订单菜品
        String orderContext = buildTodayOrderContext(userId);

        // 2. 构建RAG检索上下文（营养知识库）
        String nutritionKB = buildNutritionKB();

        // 3. 获取历史对话
        List<Conversation> history = conversationMapper.selectByUserId(userId);
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统提示
        String systemPrompt = String.format(
            "你是一个专业的AI膳食营养分析助手。你可以：\n" +
            "1. 分析用户点的菜品营养成分（热量、蛋白质、脂肪、碳水化合物）\n" +
            "2. 根据中国居民膳食指南给出搭配建议\n" +
            "3. 回答营养相关问题\n\n" +
            "当前用户今日已点菜品：\n%s\n\n" +
            "营养知识库参考：\n%s\n\n" +
            "中国居民每日推荐摄入量：热量2000千卡、蛋白质60g、脂肪65g、碳水化合物300g。\n" +
            "请用友好、专业的语气回答。回答控制在200字以内。",
            orderContext.isEmpty() ? "（今日暂无订单）" : orderContext, nutritionKB);

        Map<String, String> sysMsg = new HashMap<>();
        sysMsg.put("role", "system"); sysMsg.put("content", systemPrompt);
        messages.add(sysMsg);

        // 历史对话（取最近10轮）
        for (Conversation c : history.subList(Math.max(0, history.size() - 10), history.size())) {
            Map<String, String> m = new HashMap<>();
            m.put("role", c.getRole()); m.put("content", c.getContent());
            messages.add(m);
        }

        // 当前用户消息
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user"); userMsg.put("content", message);
        messages.add(userMsg);

        // 保存用户消息
        Conversation userConv = Conversation.builder()
            .userId(userId).role("user").content(message).createTime(LocalDateTime.now()).build();
        conversationMapper.insert(userConv);

        try {
            String aiReply = callAI(messages);
            // 保存AI回复
            Conversation aiConv = Conversation.builder()
                .userId(userId).role("assistant").content(aiReply).createTime(LocalDateTime.now()).build();
            conversationMapper.insert(aiConv);

            Map<String, Object> result = new HashMap<>();
            result.put("reply", aiReply);
            result.put("hasOrders", !orderContext.isEmpty());
            return result;
        } catch (Exception e) {
            String fallback = generateFallbackReply(orderContext);
            Conversation aiConv = Conversation.builder()
                .userId(userId).role("assistant").content(fallback).createTime(LocalDateTime.now()).build();
            conversationMapper.insert(aiConv);
            Map<String, Object> result = new HashMap<>();
            result.put("reply", fallback);
            result.put("hasOrders", !orderContext.isEmpty());
            result.put("fallback", true);
            return result;
        }
    }

    @Override
    public Map<String, Object> weeklyReport(Long userId) {
        // 获取近7天订单
        List<Order> allOrders = orderMapper.selectByUserId(userId);
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Order> recentOrders = allOrders.stream()
            .filter(o -> o.getCreateTime() != null && o.getCreateTime().isAfter(sevenDaysAgo))
            .filter(o -> o.getOrderStatus() != null && o.getOrderStatus() == 3) // 已完成的
            .collect(Collectors.toList());

        // 统计营养摄入
        Map<String, Object> stats = calculateStats(recentOrders);

        // 调用AI生成周报
        int days = Math.max(1, recentOrders.isEmpty() ? 7 : recentOrders.size());
        String prompt = String.format(
            "用户近7天共完成%d笔订单。\n" +
            "总热量摄入：%s千卡（日均%.0f千卡）\n" +
            "总蛋白质摄入：%sg（日均%.1fg）\n" +
            "总脂肪摄入：%sg（日均%.1fg）\n" +
            "总碳水摄入：%sg（日均%.1fg）\n\n" +
            "常点菜品：%s\n\n" +
            "请根据以上数据生成一份简洁的个人饮食习惯周报，包含：\n" +
            "1. 总体评价（1-2句）\n" +
            "2. 营养摄入是否均衡\n" +
            "3. 2-3条具体的饮食改善建议\n" +
            "请控制在200字以内。",
            recentOrders.size(),
            stats.get("totalCalories"), ((Number) stats.get("totalCalories")).doubleValue() / 7,
            stats.get("totalProtein"), ((Number) stats.get("totalProtein")).doubleValue() / 7,
            stats.get("totalFat"), ((Number) stats.get("totalFat")).doubleValue() / 7,
            stats.get("totalCarbs"), ((Number) stats.get("totalCarbs")).doubleValue() / 7,
            stats.get("topDishes"));

        try {
            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", "你是一个专业的饮食健康分析助手，请根据用户数据生成周报。");
            messages.add(sysMsg);
            Map<String, String> um = new HashMap<>();
            um.put("role", "user"); um.put("content", prompt);
            messages.add(um);

            String report = callAI(messages);
            Map<String, Object> result = new HashMap<>();
            result.put("report", report);
            result.put("stats", stats);
            result.put("orderCount", recentOrders.size());
            return result;
        } catch (Exception e) {
            String fallback = generateFallbackReport(stats, recentOrders.size());
            Map<String, Object> result = new HashMap<>();
            result.put("report", fallback);
            result.put("stats", stats);
            result.put("orderCount", recentOrders.size());
            result.put("fallback", true);
            return result;
        }
    }

    /** 调用AI API */
    private String callAI(List<Map<String, String>> messages) throws Exception {
        Map<String, Object> body = new HashMap<>();
        body.put("model", aiModel);
        body.put("temperature", 0.7);
        body.put("max_tokens", 600);
        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiApiKey);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(aiApiUrl, entity, String.class);
        return objectMapper.readTree(resp.getBody())
            .get("choices").get(0).get("message").get("content").asText();
    }

    /** 构建今日订单上下文 */
    private String buildTodayOrderContext(Long userId) {
        List<Order> orders = orderMapper.selectByUserId(userId);
        if (orders.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        for (Order o : orders) {
            if (o.getCreateTime() != null && o.getCreateTime().toLocalDate().equals(LocalDateTime.now().toLocalDate())) {
                List<OrderDetail> details = orderDetailMapper.selectByOrderId(o.getId());
                for (OrderDetail d : details) {
                    Dish dish = dishMapper.selectById(d.getDishId());
                    if (dish != null) {
                        sb.append("- ").append(dish.getDishName())
                          .append(" x").append(d.getQuantity())
                          .append("（热量").append(dish.getCalories() != null ? dish.getCalories() : "?")
                          .append("千卡/100g，蛋白质").append(dish.getProtein() != null ? dish.getProtein() : "?")
                          .append("g，脂肪").append(dish.getFat() != null ? dish.getFat() : "?")
                          .append("g，碳水").append(dish.getCarbs() != null ? dish.getCarbs() : "?").append("g）\n");
                    }
                }
            }
        }
        return sb.toString();
    }

    /** 构建营养知识库（RAG检索源） */
    private String buildNutritionKB() {
        List<Dish> dishes = dishMapper.selectAll();
        StringBuilder sb = new StringBuilder();
        for (Dish d : dishes) {
            sb.append(d.getDishName()).append("：")
              .append("热量").append(d.getCalories() != null ? d.getCalories() : 0).append("千卡/100g，")
              .append("蛋白质").append(d.getProtein() != null ? d.getProtein() : 0).append("g，")
              .append("脂肪").append(d.getFat() != null ? d.getFat() : 0).append("g，")
              .append("碳水").append(d.getCarbs() != null ? d.getCarbs() : 0).append("g\n");
        }
        return sb.toString();
    }

    /** 统计营养数据 */
    private Map<String, Object> calculateStats(List<Order> orders) {
        int totalCal = 0;
        BigDecimal totalProt = BigDecimal.ZERO, totalFat = BigDecimal.ZERO, totalCarbs = BigDecimal.ZERO;
        Map<String, Integer> dishCount = new HashMap<>();

        for (Order o : orders) {
            List<OrderDetail> details = orderDetailMapper.selectByOrderId(o.getId());
            for (OrderDetail d : details) {
                Dish dish = dishMapper.selectById(d.getDishId());
                if (dish != null) {
                    if (dish.getCalories() != null) totalCal += dish.getCalories() * d.getQuantity();
                    if (dish.getProtein() != null) totalProt = totalProt.add(dish.getProtein().multiply(BigDecimal.valueOf(d.getQuantity())));
                    if (dish.getFat() != null) totalFat = totalFat.add(dish.getFat().multiply(BigDecimal.valueOf(d.getQuantity())));
                    if (dish.getCarbs() != null) totalCarbs = totalCarbs.add(dish.getCarbs().multiply(BigDecimal.valueOf(d.getQuantity())));
                    dishCount.merge(dish.getDishName(), d.getQuantity(), Integer::sum);
                }
            }
        }

        // Top3常点菜品
        String top = dishCount.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .limit(3).map(e -> e.getKey() + "(" + e.getValue() + "次)").collect(Collectors.joining("、"));

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCalories", totalCal);
        stats.put("totalProtein", totalProt.setScale(1, RoundingMode.HALF_UP));
        stats.put("totalFat", totalFat.setScale(1, RoundingMode.HALF_UP));
        stats.put("totalCarbs", totalCarbs.setScale(1, RoundingMode.HALF_UP));
        stats.put("topDishes", top.isEmpty() ? "暂无" : top);
        return stats;
    }

    /** 兜底回复 */
    private String generateFallbackReply(String orderContext) {
        if (orderContext.isEmpty()) return "您今天还没有下单哦～点完餐后可以来问我营养搭配问题！";
        return "根据中国居民膳食指南，建议每餐搭配主食、蛋白质和蔬菜，保持营养均衡。您今日已点的菜品看起来不错，如需详细营养分析请稍后再试～";
    }

    private String generateFallbackReport(Map<String, Object> stats, int count) {
        return String.format(
            "📊 您的7天饮食周报\n\n" +
            "本周共完成%d笔订单\n" +
            "总热量：%s千卡（日均%.0f千卡）\n" +
            "蛋白质：%sg | 脂肪：%sg | 碳水：%sg\n\n" +
            "常点菜品：%s\n\n" +
            "💡 建议：保持饮食多样化，注意荤素搭配，多摄入蔬菜水果。",
            count, stats.get("totalCalories"),
            ((Number) stats.get("totalCalories")).doubleValue() / 7,
            stats.get("totalProtein"), stats.get("totalFat"), stats.get("totalCarbs"),
            stats.get("topDishes"));
    }
}
