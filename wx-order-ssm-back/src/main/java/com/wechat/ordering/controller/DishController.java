package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Dish;
import com.wechat.ordering.service.DishService;
import com.wechat.ordering.util.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/admin/dishes")
public class DishController {

    @Autowired
    private DishService dishService;

    @Value("${ai.api.url}") private String aiApiUrl;
    @Value("${ai.api.key}") private String aiApiKey;
    @Value("${ai.api.model}") private String aiModel;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** AI智能查询菜品营养信息 */
    @PostMapping("/nutrition-lookup")
    public Result<Map<String, Object>> nutritionLookup(@RequestBody Map<String, String> params) {
        String dishName = params.get("dishName");
        if (dishName == null || dishName.isEmpty()) return Result.error("菜品名称不能为空");

        String prompt = String.format(
            "请查询菜品\"%s\"的营养成分（每100g含量），严格按以下JSON格式返回（不要包含markdown标记）：\n" +
            "{\"calories\":热量千卡数,\"protein\":蛋白质克数,\"fat\":脂肪克数,\"carbs\":碳水化合物克数}\n" +
            "只返回JSON，不要其他内容。", dishName);

        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", aiModel);
            body.put("temperature", 0.3);
            body.put("max_tokens", 200);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> msg = new HashMap<>();
            msg.put("role", "user"); msg.put("content", prompt);
            messages.add(msg);
            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + aiApiKey);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<String> resp = restTemplate.postForEntity(aiApiUrl, entity, String.class);
            String content = objectMapper.readTree(resp.getBody())
                .get("choices").get(0).get("message").get("content").asText();

            if (content.startsWith("```")) content = content.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");

            Map<String, Object> result = objectMapper.readValue(content, Map.class);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("AI查询失败，请手动填写营养信息");
        }
    }

    @GetMapping
    public Result<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        return Result.success(dishService.list(page, pageSize, categoryId, keyword));
    }

    @GetMapping("/{id}")
    public Result<Dish> getById(@PathVariable Long id) {
        Dish dish = dishService.getById(id);
        if (dish == null) {
            return Result.error("菜品不存在");
        }
        return Result.success(dish);
    }

    @PostMapping
    public Result<?> add(@RequestBody Dish dish) {
        dishService.add(dish);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestBody Dish dish) {
        dish.setId(id);
        dishService.update(dish);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params) {
        try {
            dishService.updateStatus(id, params.get("status"));
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        dishService.delete(id);
        return Result.success();
    }
}
