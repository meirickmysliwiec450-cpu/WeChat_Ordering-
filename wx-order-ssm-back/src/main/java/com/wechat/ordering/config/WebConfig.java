package com.wechat.ordering.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Spring MVC 配置 - 跨域请求支持 + 认证拦截器 + 图片静态资源
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private WxAuthInterceptor wxAuthInterceptor;

    /** 图片目录绝对路径 */
    private static final String IMAGES_DIR = System.getProperty("user.dir")
        + "/../wx-order-frontend-user1/public/images";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 用 Path.toUri() 生成 file:/// URL，它会自动处理中文路径编码
        Path imagePath = Paths.get(IMAGES_DIR).toAbsolutePath().normalize();
        registry.addResourceHandler("/images/**")
                .addResourceLocations(imagePath.toUri().toString());
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
                .excludePathPatterns("/admin/auth/login");
        // 小程序端认证拦截器
        registry.addInterceptor(wxAuthInterceptor)
                .addPathPatterns("/wx/**")
                .excludePathPatterns("/wx/user/login");
    }
}
