package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.*;
import com.wechat.ordering.mapper.*;
import com.wechat.ordering.service.RecommendService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendServiceImpl implements RecommendService {

    @Value("${ai.api.url}")
    private String aiApiUrl;

    @Value("${ai.api.key}")
    private String aiApiKey;

    @Value("${ai.api.model}")
    private String aiModel;

    @Autowired private OrderMapper orderMapper;
    @Autowired private OrderDetailMapper orderDetailMapper;
    @Autowired private DishMapper dishMapper;
    @Autowired private CategoryMapper categoryMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Map<String, Object> recommend(Long userId) {
        // 1. 获取用户历史订单
        List<Order> orders = orderMapper.selectByUserId(userId);
        Map<Long, Integer> dishCount = new HashMap<>();
        for (Order o : orders) {
            List<OrderDetail> details = orderDetailMapper.selectByOrderId(o.getId());
            for (OrderDetail d : details) {
                dishCount.merge(d.getDishId(), d.getQuantity(), Integer::sum);
            }
        }

        // 2. 判断当前时段
        int hour = LocalTime.now().getHour();
        String period;
        if (hour >= 6 && hour < 10) period = "早餐";
        else if (hour >= 10 && hour < 14) period = "午餐";
        else if (hour >= 14 && hour < 17) period = "下午茶";
        else if (hour >= 17 && hour < 21) period = "晚餐";
        else period = "夜宵";

        // 3. 获取所有上架菜品作为候选
        List<Dish> allDishes = dishMapper.selectByStatus(1);
        Map<Long, Category> categoryMap = new HashMap<>();
        for (Category c : categoryMapper.selectAll()) categoryMap.put(c.getId(), c);

        // 4. 构建历史记录描述
        StringBuilder history = new StringBuilder();
        if (dishCount.isEmpty()) {
            history.append("（新用户，暂无历史点餐记录）\n");
        } else {
            dishCount.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .forEach(e -> {
                    Dish dish = dishMapper.selectById(e.getKey());
                    if (dish != null) {
                        Category cat = categoryMap.get(dish.getCategoryId());
                        String catName = cat != null ? cat.getCategoryName() : "";
                        history.append("- ").append(dish.getDishName())
                               .append("（").append(catName).append("）- ")
                               .append(e.getValue()).append("次\n");
                    }
                });
        }

        // 5. 构建候选菜品列表
        StringBuilder candidates = new StringBuilder();
        for (Dish d : allDishes) {
            Category cat = categoryMap.get(d.getCategoryId());
            String catName = cat != null ? cat.getCategoryName() : "";
            candidates.append("- ").append(d.getDishName())
                      .append("（").append(catName)
                      .append("，¥").append(d.getPrice()).append("）\n");
        }

        // 6. 调用大模型API
        String prompt = String.format(
            "你是一个美食推荐助手。根据用户的历史点餐记录和当前时段，从候选菜品中推荐3道菜品。\n\n" +
            "当前时段：%s\n\n" +
            "用户历史点餐：\n%s\n" +
            "候选菜品：\n%s\n" +
            "请根据以下原则推荐：\n" +
            "1. 结合历史偏好，推荐用户可能喜欢但还没吃腻的\n" +
            "2. 考虑当前时段，推荐合适的菜品（早餐清淡、午餐营养、晚餐适量）\n" +
            "3. 适当推荐不同分类的菜品，营养搭配\n\n" +
            "请严格按JSON格式返回（不要包含markdown代码块标记）：\n" +
            "{\"dishes\":[{\"dishName\":\"菜品名\",\"reason\":\"推荐理由\"},...]}",
            period, history.toString(), candidates.toString()
        );

        try {
            Map<String, Object> reqBody = new HashMap<>();
            reqBody.put("model", aiModel);
            reqBody.put("temperature", 0.7);
            reqBody.put("max_tokens", 800);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", prompt);
            messages.add(userMsg);
            reqBody.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(reqBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(aiApiUrl, entity, String.class);
            String content = objectMapper.readTree(response.getBody())
                .get("choices").get(0).get("message").get("content").asText();

            // 解析JSON（去除可能的markdown标记）
            if (content.startsWith("```")) {
                content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
            }

            Map<String, Object> aiResult = objectMapper.readValue(content, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> aiDishes = (List<Map<String, Object>>) aiResult.get("dishes");

            // 7. 匹配实际菜品（按名称模糊匹配）
            List<Map<String, Object>> recommendList = new ArrayList<>();
            for (Map<String, Object> aiDish : aiDishes) {
                String name = (String) aiDish.get("dishName");
                String reason = (String) aiDish.get("reason");
                for (Dish d : allDishes) {
                    if (d.getDishName().contains(name) || name.contains(d.getDishName())) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", d.getId());
                        item.put("dishName", d.getDishName());
                        item.put("image", d.getImage());
                        item.put("price", d.getPrice());
                        item.put("sales", d.getSales());
                        item.put("reason", reason != null ? reason : "根据您的口味偏好推荐");
                        Category cat = categoryMap.get(d.getCategoryId());
                        if (cat != null) item.put("categoryName", cat.getCategoryName());
                        recommendList.add(item);
                        break;
                    }
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("period", period);
            result.put("dishes", recommendList);
            result.put("userName", "");
            return result;

        } catch (Exception e) {
            // API调用失败时返回兜底推荐
            return fallbackRecommend(allDishes, categoryMap, period, dishCount);
        }
    }

    /** 兜底推荐：API失败时根据历史记录简单推荐 */
    private Map<String, Object> fallbackRecommend(List<Dish> allDishes, Map<Long, Category> categoryMap,
                                                   String period, Map<Long, Integer> dishCount) {
        List<Map<String, Object>> recommendList = new ArrayList<>();
        // 优先推荐高销量但用户还没点过的
        List<Dish> sorted = allDishes.stream()
            .sorted((a, b) -> Integer.compare(
                b.getSales() != null ? b.getSales() : 0,
                a.getSales() != null ? a.getSales() : 0))
            .collect(Collectors.toList());

        int added = 0;
        // 先推荐用户没点过的热销菜品
        for (Dish d : sorted) {
            if (added >= 3) break;
            if (!dishCount.containsKey(d.getId())) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", d.getId());
                item.put("dishName", d.getDishName());
                item.put("image", d.getImage());
                item.put("price", d.getPrice());
                item.put("sales", d.getSales());
                item.put("reason", "热销推荐 · 适合" + period + "享用");
                Category cat = categoryMap.get(d.getCategoryId());
                if (cat != null) item.put("categoryName", cat.getCategoryName());
                recommendList.add(item);
                added++;
            }
        }
        // 不够3个，补充用户点过的
        for (Dish d : sorted) {
            if (added >= 3) break;
            boolean alreadyIn = recommendList.stream().anyMatch(r -> r.get("id").equals(d.getId()));
            if (!alreadyIn) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", d.getId());
                item.put("dishName", d.getDishName());
                item.put("image", d.getImage());
                item.put("price", d.getPrice());
                item.put("sales", d.getSales());
                item.put("reason", "根据您的口味偏好推荐");
                Category cat = categoryMap.get(d.getCategoryId());
                if (cat != null) item.put("categoryName", cat.getCategoryName());
                recommendList.add(item);
                added++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("period", period);
        result.put("dishes", recommendList);
        result.put("userName", "");
        result.put("fallback", true);
        return result;
    }
}
