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

    /** 将相对路径解析为完整URL */
    public static String resolveImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) return imagePath;
        if (imagePath.startsWith("data:")) return imagePath;

        // 如果存的是本系统的绝对URL（换了IP/localhost），提取相对路径后重新拼接当前base-url
        if (imagePath.startsWith("http://") || imagePath.startsWith("https://")) {
            int idx = imagePath.indexOf("/images/");
            if (idx > 0) {
                // 提取 /images/... 相对路径
                String relative = imagePath.substring(idx);
                String base = INSTANCE.baseUrl;
                if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
                return base + relative;
            }
            // 外部URL，原样返回
            return imagePath;
        }

        String base = INSTANCE.baseUrl;
        if (base.endsWith("/")) base = base.substring(0, base.length() - 1);
        if (!imagePath.startsWith("/")) imagePath = "/" + imagePath;
        return base + imagePath;
    }

    /** 小程序专用：返回 base64 图片 API URL，绕过微信 HTTP 限制 */
    public static String resolveImageBase64(String imagePath) {
        String url = resolveImage(imagePath);
        if (url == null || url.isEmpty()) return url;
        if (url.startsWith("data:")) return url;
        return url + "?base64=true";
    }

    /** 获取 baseUrl（非静态调用） */
    public String getBaseUrl() {
        return baseUrl;
    }
}
