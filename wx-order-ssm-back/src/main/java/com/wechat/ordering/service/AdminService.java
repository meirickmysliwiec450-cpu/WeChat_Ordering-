package com.wechat.ordering.service;

import com.wechat.ordering.entity.Admin;
import java.util.Map;

public interface AdminService {
    /** 管理员登录，返回 token 和管理员信息 */
    Map<String, Object> login(String username, String password);
    /** 根据ID获取管理员信息 */
    Admin getById(Long id);
    /** 更新管理员信息 */
    void update(Admin admin);
}
