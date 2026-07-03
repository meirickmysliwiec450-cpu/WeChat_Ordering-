package com.wechat.ordering.service.impl;

import com.wechat.ordering.config.AppConfig;
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

        // 5. 构建候选菜品列表（含描述和营养信息，让AI有料可写）
        StringBuilder candidates = new StringBuilder();
        for (Dish d : allDishes) {
            Category cat = categoryMap.get(d.getCategoryId());
            String catName = cat != null ? cat.getCategoryName() : "";
            candidates.append("- ").append(d.getDishName())
                      .append("【").append(catName)
                      .append("，¥").append(d.getPrice())
                      .append("，月销").append(d.getSales() != null ? d.getSales() : 0).append("单");
            if (d.getDescription() != null && !d.getDescription().isEmpty()) {
                candidates.append("，").append(d.getDescription());
            }
            if (d.getCalories() != null && d.getCalories() > 0) {
                candidates.append("，热量").append(d.getCalories()).append("千卡");
            }
            candidates.append("】\n");
        }

        // 6. 调用大模型API
        String prompt = String.format(
            "你是一位资深美食评论家，请根据用户的历史点餐记录和当前时段，从候选菜品中精心推荐3道菜品。\n\n" +
            "当前时段：%s\n\n" +
            "用户历史点餐：\n%s\n" +
            "候选菜品：\n%s\n" +
            "推荐原则：\n" +
            "1. 结合历史偏好，推荐用户可能喜欢但还没吃腻的\n" +
            "2. 考虑当前时段，推荐合适的菜品\n" +
            "3. 3道菜品应来自不同分类，营养搭配均衡\n\n" +
            "推荐理由写作要求：\n" +
            "- 【风格差异】3条推荐理由必须风格迥异、各有千秋，严禁套用相同句式模板\n" +
            "- 【紧扣食材】从菜品本身的食材、做法、口感、风味入手，每条理由要有独特的切入角度\n" +
            "- 【文笔优美】用美食点评的口吻写作，善用感官描写（酥脆、鲜嫩、浓郁、清爽等），让文字有画面感\n" +
            "- 【真情实感】写得像一位懂吃的老饕在真诚分享，而非冰冷的机器推荐\n" +
            "- 【字数】每条30~60字\n\n" +
            "风格示例：\n" +
            "- \"您之前钟情于川菜的麻辣鲜香，这道水煮鱼将花椒的麻与辣椒的香完美融合，鱼片嫩滑入口即化。午间来一份，开胃又提神，搭配一碗米饭便是人间至味。\"\n" +
            "- \"清晨的胃需要温柔的唤醒——这份皮蛋瘦肉粥熬得绵密浓稠，皮蛋的醇香与瘦肉的鲜甜交相辉映，暖胃又暖心，是早餐的不二之选。\"\n" +
            "- \"试过这道糖醋里脊吗？外酥里嫩的金黄肉条裹着酸甜适口的酱汁，一口下去咔嚓作响，幸福感瞬间拉满。作为晚餐的硬菜，搭配清炒时蔬营养刚刚好。\"\n\n" +
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
                        item.put("image", AppConfig.resolveImage(d.getImage()));
                        item.put("price", d.getPrice());
                        item.put("sales", d.getSales());
                        item.put("reason", reason != null ? reason : buildFallbackReason(d, categoryMap, period, "hot"));
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
                item.put("image", AppConfig.resolveImage(d.getImage()));
                item.put("price", d.getPrice());
                item.put("sales", d.getSales());
                item.put("reason", buildFallbackReason(d, categoryMap, period, "hot"));
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
                item.put("image", AppConfig.resolveImage(d.getImage()));
                item.put("price", d.getPrice());
                item.put("sales", d.getSales());
                item.put("reason", buildFallbackReason(d, categoryMap, period, "history"));
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

    /** 构建兜底推荐理由（多样化模板，避免雷同） */
    private String buildFallbackReason(Dish dish, Map<Long, Category> categoryMap,
                                        String period, String source) {
        Category cat = categoryMap.get(dish.getCategoryId());
        String catName = cat != null ? cat.getCategoryName() : "菜品";
        String name = dish.getDishName();
        int sales = dish.getSales() != null ? dish.getSales() : 0;

        // 时段场景描写（多样化的意象表达）
        String[][] periodScenes = {
            {"早餐", "清晨的第一缕阳光", "唤醒沉睡的味蕾", "开启元气满满的一天", "晨光熹微，"},
            {"午餐", "正午时分", "犒劳忙碌了一上午的自己", "为下午充满电", "午间小憩，"},
            {"下午茶", "午后时光慵懒而惬意", "给疲惫的身心放个假", "偷得浮生半日闲", "午后暖阳下，"},
            {"晚餐", "夜幕降临华灯初上", "卸下一身的疲惫", "用美食治愈一天的辛劳", "黄昏时分，"},
            {"夜宵", "夜深人静时分", "给夜晚添一抹温柔", "夜深了，对自己好一点", "夜色温柔，"}
        };
        String[] scene = null;
        for (String[] s : periodScenes) {
            if (s[0].equals(period)) { scene = s; break; }
        }
        if (scene == null) scene = new String[]{"", "此刻", "犒劳一下自己", "享受当下的美好", ""};

        // 用 dish.id 取模选模板，确保不同菜品用不同模板
        int t = (int) (dish.getId() % 6);

        if ("hot".equals(source)) {
            // 热销/新发现类推荐 —— 6种迥异风格
            switch (t) {
                case 0:
                    return String.format("🔥 %s这份%s已经征服了%d位食客的胃。%s来上一份，酥香鲜嫩在舌尖绽放，治愈力满分。",
                        scene[4], name, sales, scene[1]);
                case 1:
                    return String.format("✨ 想吃点特别的？%s——%s中的隐藏宝藏，%s恰到好处地%s。一口下去便知何为\"值得\"。",
                        name, catName, scene[1], scene[2]);
                case 2:
                    return String.format("🌟 %s在%s分类中人气爆棚并非偶然——用料讲究、火候到位，%s%s，让平凡的日子也变得闪闪发光。",
                        name, catName, scene[4], scene[3]);
                case 3:
                    return String.format("💫 口碑相传的%s，累计售出%d份的好味道。%s，用一道好菜%s吧。",
                        name, sales, scene[1], scene[2]);
                case 4:
                    return String.format("🎯 还没试过%s？这道%s精选之作，%s来一份，感受食材最本真的鲜美与满足。",
                        name, catName, scene[1]);
                default:
                    return String.format("🍽️ %s——%s中脱颖而出的实力派，%s为身体注入能量的同时，更是一场味觉的小确幸。",
                        name, catName, scene[1]);
            }
        } else {
            // 历史偏好类推荐 —— 6种迥异风格
            switch (t) {
                case 0:
                    return String.format("💚 您与%s的故事我们一直记得。%s再次与这份熟悉的美味相遇，像老朋友一样温暖而妥帖。",
                        name, scene[4]);
                case 1:
                    return String.format("🥢 %s——您餐桌上的常客。%s重温经典，熟悉的味道总能带来最踏实的幸福感。",
                        name, scene[1]);
                case 2:
                    return String.format("😋 念念不忘，必有回响。%s%s来一份，每一口都是记忆里那个让人微笑的味道。",
                        scene[4], name);
                case 3:
                    return String.format("🏮 您钟爱的%s，%s再点一次又何妨？好东西值得反复品味，经典永不过时。",
                        name, scene[1]);
                case 4:
                    return String.format("🌿 %s——熟悉又安心的选择。%s，让这份温暖的味道%s。",
                        name, scene[4], scene[2]);
                default:
                    return String.format("💝 时间验证过的美味——%s。%s再来一份，有些幸福就是这么简单而确定。",
                        name, scene[1]);
            }
        }
    }
}
