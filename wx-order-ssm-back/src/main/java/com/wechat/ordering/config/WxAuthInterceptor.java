package com.wechat.ordering.config;

import com.wechat.ordering.util.JwtUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 小程序端用户认证拦截器
 */
@Component
public class WxAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        System.out.println("========== WxAuthInterceptor 拦截请求 ==========");
        System.out.println("请求路径：" + request.getRequestURI());
        System.out.println("请求方法：" + request.getMethod());
        
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            System.out.println("OPTIONS请求，放行");
            System.out.println("================================================");
            return true;
        }

        String path = request.getRequestURI();
        // 登录接口放行
        if (path.contains("/wx/user/login")) {
            System.out.println("登录接口，放行");
            System.out.println("================================================");
            return true;
        }

        String token = request.getHeader("Authorization");
        System.out.println("原始Authorization header：" + token);
        
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        System.out.println("提取后的Token：" + (token != null ? (token.length() > 50 ? token.substring(0, 50) + "..." : token) : "null"));

        if (token == null || !JwtUtil.validate(token)) {
            System.out.println("⚠️ Token无效或不存在！返回401");
            System.out.println("  - token为null：" + (token == null));
            System.out.println("  - JwtUtil.validate结果：" + (token != null ? JwtUtil.validate(token) : false));
            System.out.println("================================================");
            
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或Token已过期\"}");
            return false;
        }

        Long userId = JwtUtil.getUserId(token);
        System.out.println("✓ Token验证成功！userId = " + userId);
        request.setAttribute("userId", userId);
        System.out.println("================================================");
        return true;
    }
}
