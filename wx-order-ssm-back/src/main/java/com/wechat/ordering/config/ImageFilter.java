package com.wechat.ordering.config;

import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * 图片服务过滤器 —— 在请求进入 Spring MVC 之前直接处理 /images/ 请求
 * 彻底避开所有路由匹配和资源映射的问题
 */
@Component
public class ImageFilter implements Filter {

    /** 图片根目录 —— 硬编码绝对路径，不依赖 user.dir，路径无中文 */
    private static final File IMG_ROOT = new File("E:/wechat_images");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();          // /api/images/noodles/test.png
        String ctx = request.getContextPath();          // /api
        String prefix = ctx + "/images/";               // /api/images/

        // 不是图片请求，放行给 Spring MVC
        if (!uri.startsWith(prefix) || uri.length() <= prefix.length()) {
            chain.doFilter(req, res);
            return;
        }

        // 提取相对路径：/api/images/noodles/test.png → noodles/test.png
        String relativePath = uri.substring(prefix.length());

        // 安全检查
        if (relativePath.contains("..") || relativePath.contains("\\")) {
            response.sendError(403);
            return;
        }

        File file = new File(IMG_ROOT, relativePath);
        if (!file.exists() || !file.isFile()) {
            response.sendError(404);
            return;
        }

        // 设置 Content-Type
        String name = file.getName().toLowerCase();
        if (name.endsWith(".png")) response.setContentType("image/png");
        else if (name.endsWith(".jpg") || name.endsWith(".jpeg")) response.setContentType("image/jpeg");
        else if (name.endsWith(".gif")) response.setContentType("image/gif");
        else if (name.endsWith(".webp")) response.setContentType("image/webp");
        else response.setContentType("application/octet-stream");

        // 输出文件内容
        try (FileInputStream fis = new FileInputStream(file);
             OutputStream os = response.getOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = fis.read(buf)) != -1) {
                os.write(buf, 0, n);
            }
            os.flush();
        }
    }
}
