package com.wechat.ordering.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Spring MVC 配置 - 跨域请求支持 + 认证拦截器 + 图片静态资源
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private WxAuthInterceptor wxAuthInterceptor;

    /** 前端 public/images 的绝对路径 */
    private static final String IMAGES_PATH =
        System.getProperty("user.dir") + "/../wx-order-frontend-user1/public/images";


    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // ========== 原有代码 完全不动 ==========
        String absolutePath = new File(IMAGES_PATH).getAbsolutePath();
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + absolutePath + "/");

        // ========== 新增：上传图片/upload 映射（核心修复） ==========
        // 访问路径前缀 /upload/** 对应磁盘 D:/upload/
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:D:/upload/");
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 管理端认证拦截器
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/auth/login","/upload/**");
        // 小程序端认证拦截器
        registry.addInterceptor(wxAuthInterceptor)
                .addPathPatterns("/wx/**")
                .excludePathPatterns("/wx/user/login","/upload/**");
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
