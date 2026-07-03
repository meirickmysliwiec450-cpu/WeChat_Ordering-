package com.wechat.ordering.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 应用全局配置 - 提供图片等静态资源的完整访问URL
 *
 * 使用方式：在需要返回图片URL的地方调用 AppConfig.resolveImage(path)
 * 队友联调时修改 application.properties 中的 app.base-url 为自己的局域网IP即可
 */
@Component
public class AppConfig {

    private static AppConfig INSTANCE;

    @Value("${app.base-url}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        INSTANCE = this;
    }

    /**
     * 将相对路径（如 /images/面食/饺子.png）解析为完整URL
     * 如果已经是完整URL则直接返回
     */
    public static String resolveImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return imagePath;
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) return imagePath;
        // 去掉可能重复的 /api 前缀
        String base = INSTANCE.baseUrl;
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (!imagePath.startsWith("/")) imagePath = "/" + imagePath;
        return base + imagePath;
    }

    /** 获取 baseUrl（非静态调用） */
    public String getBaseUrl() {
        return baseUrl;
    }
}
