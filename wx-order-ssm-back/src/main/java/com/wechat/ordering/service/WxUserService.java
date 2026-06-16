package com.wechat.ordering.service;

import com.wechat.ordering.entity.User;
import java.util.Map;

public interface WxUserService {
    /** 微信登录/注册，返回token和用户信息 */
    Map<String, Object> login(String openId, String nickName, String avatar, Integer gender);
    /** 获取用户信息 */
    User getInfo(Long userId);
    /** 更新用户信息 */
    void updateInfo(Long userId, User user);
}
