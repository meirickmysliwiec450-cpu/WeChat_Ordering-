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

    /** 图片根目录 */
    private static final File IMG_ROOT = new File("E:/wechat_images");

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String uri = request.getRequestURI();          // /api/images/noodles/test.png
        String ctx = request.getContextPath();          // /api
        String prefix = ctx + "/images/";               // /api/images/

        System.out.println("========== ImageFilter 收到请求 ==========");
        System.out.println("请求URI：" + uri);
        System.out.println("ContextPath：" + ctx);
        System.out.println("图片根目录：" + IMG_ROOT.getAbsolutePath());
        System.out.println("图片根目录是否存在：" + IMG_ROOT.exists());

        // 不是图片请求，放行给 Spring MVC
        if (!uri.startsWith(prefix) || uri.length() <= prefix.length()) {
            System.out.println("不是图片请求，放行");
            System.out.println("====================================");
            chain.doFilter(req, res);
            return;
        }

        // 提取相对路径：/api/images/noodles/test.png → noodles/test.png
        String relativePath = uri.substring(prefix.length());
        System.out.println("提取的相对路径：" + relativePath);

        // 安全检查
        if (relativePath.contains("..") || relativePath.contains("\\")) {
            System.out.println("路径不安全，返回403");
            System.out.println("====================================");
            response.sendError(403);
            return;
        }

        File file = new File(IMG_ROOT, relativePath);
        System.out.println("完整文件路径：" + file.getAbsolutePath());
        System.out.println("文件是否存在：" + file.exists());
        System.out.println("是否是文件：" + file.isFile());

        if (!file.exists() || !file.isFile()) {
            System.out.println("文件不存在或不是文件，返回404");
            System.out.println("====================================");
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

        System.out.println("文件存在，开始输出图片");
        System.out.println("====================================");

        // 检测是否请求 base64
        boolean base64 = "true".equals(request.getParameter("base64"));
        if (base64) {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] data = new byte[(int) file.length()];
                fis.read(data);
                String b64 = java.util.Base64.getEncoder().encodeToString(data);
                String mime = response.getContentType();
                response.setContentType("text/plain");
                response.getWriter().write("data:" + mime + ";base64," + b64);
            }
            return;
        }

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
