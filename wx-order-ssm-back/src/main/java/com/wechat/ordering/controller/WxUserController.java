package com.wechat.ordering.controller;

import com.wechat.ordering.entity.User;
import com.wechat.ordering.service.WxUserService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/wx/user")
public class WxUserController {

    @Autowired
    private WxUserService wxUserService;

    /** 微信登录/注册 */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> params) {
        String openId = (String) params.get("openId");
        if (openId == null || openId.isEmpty()) return Result.error("openId不能为空");
        String nickName = (String) params.get("nickName");
        String avatar = (String) params.get("avatar");
        Integer gender = params.get("gender") != null ? (Integer) params.get("gender") : null;
        return Result.success(wxUserService.login(openId, nickName, avatar, gender));
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
