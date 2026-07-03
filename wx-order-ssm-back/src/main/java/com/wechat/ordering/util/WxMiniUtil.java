package com.wechat.ordering.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.Resource;
import java.util.Map;

@Component
public class WxMiniUtil {
    @Value("${wx.mini.appId}")
    private String appId;
    @Value("${wx.mini.appSecret}")
    private String appSecret;

    @Resource
    private RestTemplate restTemplate;

    // Spring自带的JSON解析器，web依赖自带，不用额外maven包
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Map<String, String> getSessionByCode(String code) throws Exception {
        String url = "https://api.weixin.qq.com/sns/jscode2session"
                + "?appid=" + appId
                + "&secret=" + appSecret
                + "&js_code=" + code
                + "&grant_type=authorization_code";

        // 先接收纯文本字符串，避开text/plain转换器报错
        ResponseEntity<String> responseEntity = restTemplate.getForEntity(url, String.class);
        String jsonText = responseEntity.getBody();
        System.out.println("微信返回原始数据：" + jsonText);

        // 原生jackson转Map，无任何第三方依赖
        return objectMapper.readValue(jsonText, Map.class);
    }
}