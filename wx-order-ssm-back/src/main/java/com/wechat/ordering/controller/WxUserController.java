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
        try {
            // 1. 获取前端传来的code
            String code = (String) params.get("code");
            if (code == null || code.isEmpty()) {
                return Result.error("登录code不能为空");
            }
            // 2. 调用微信接口拿openId
            Map<String, String> wxSession = wxMiniUtil.getSessionByCode(code);
            String openId = wxSession.get("openid");
            if (openId == null || openId.isEmpty()) {
                return Result.error("获取openId失败，微信返回信息：" + wxSession);
            }
            // 3. 处理用户信息
            String nickName = (String) params.get("nickName");
            String avatar = (String) params.get("avatar");
            Integer gender = params.get("gender") != null ? (Integer) params.get("gender") : null;
            // 4. 执行业务登录
            return Result.success(wxUserService.login(openId, nickName, avatar, gender));
        } catch (Exception e) {
            // 打印完整报错，方便你看问题
            e.printStackTrace();
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
