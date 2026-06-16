package com.wechat.ordering.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置 - 跨域请求支持 + 认证拦截器
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private WxAuthInterceptor wxAuthInterceptor;

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
