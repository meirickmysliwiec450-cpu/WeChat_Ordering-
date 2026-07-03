package com.wechat.ordering.config;

import com.wechat.ordering.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class UploadWebConfig implements WebMvcConfigurer {
    @Value("${upload.local.root}")
    private String localRootPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 访问路径 /upload/** 映射到本地磁盘上传目录
        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:" + localRootPath + File.separator);
    }
}