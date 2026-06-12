package com.wechat.ordering.service;

import com.wechat.ordering.entity.User;
import java.util.List;
import java.util.Map;

public interface UserService {
    /** 分页查询用户列表 */
    Map<String, Object> list(Integer page, Integer pageSize);
    /** 根据ID查询 */
    User getById(Long id);
    /** 更新用户信息 */
    void update(User user);
    /** 更新用户状态 */
    void updateStatus(Long id, Integer status);
}
