package com.wechat.ordering.controller;

import com.wechat.ordering.entity.User;
import com.wechat.ordering.service.WxUserService;
import com.wechat.ordering.util.Result;
import com.wechat.ordering.util.WxMiniUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/wx/user")
public class WxUserController {

    @Autowired
    private WxUserService wxUserService;
    @Autowired
    private WxMiniUtil wxMiniUtil;

    /** 微信登录/注册：前端传code，后端换取openId */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> params) {
        System.out.println("========== 收到登录请求 ==========");
        System.out.println("请求参数：" + params);
        
        try {
            String openId = null;
            
            // 1. 先检查是否有直接传入的 openId（用于开发测试/答辩演示）
            String testOpenId = (String) params.get("openId");
            if (testOpenId != null && !testOpenId.isEmpty()) {
                System.out.println("✓ 使用测试openId登录：" + testOpenId);
                openId = testOpenId;
            } else {
                // 2. 正式微信登录流程
                String code = (String) params.get("code");
                if (code == null || code.isEmpty()) {
                    return Result.error("登录code不能为空");
                }
                // 调用微信接口拿openId
                Map<String, String> wxSession = wxMiniUtil.getSessionByCode(code);
                openId = wxSession.get("openid");
                if (openId == null || openId.isEmpty()) {
                    return Result.error("获取openId失败，微信返回信息：" + wxSession);
                }
                System.out.println("✓ 微信登录成功，openId：" + openId);
            }
            
            // 3. 处理用户信息
            String nickName = (String) params.get("nickName");
            String avatar = (String) params.get("avatar");
            Integer gender = params.get("gender") != null ? (Integer) params.get("gender") : null;
            
            System.out.println("用户信息 - nickName: " + nickName + ", avatar: " + avatar);
            
            // 4. 执行业务登录
            Map<String, Object> result = wxUserService.login(openId, nickName, avatar, gender);
            System.out.println("✓ 登录成功，返回结果：" + result);
            System.out.println("========== 登录完成 ==========");
            
            return Result.success(result);
        } catch (Exception e) {
            // 打印完整报错，方便你看问题
            System.out.println("✗ 登录异常：");
            e.printStackTrace();
            System.out.println("========== 登录失败 ==========");
            return Result.error("登录异常：" + e.getMessage());
        }
    }

    /** 获取用户信息 */
    @GetMapping("/info")
    public Result<User> getInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(wxUserService.getInfo(userId));
    }

    /** 更新用户信息 */
    @PutMapping("/info")
    public Result<?> updateInfo(HttpServletRequest request, @RequestBody User user) {
        Long userId = (Long) request.getAttribute("userId");
        wxUserService.updateInfo(userId, user);
        return Result.success();
    }
}
