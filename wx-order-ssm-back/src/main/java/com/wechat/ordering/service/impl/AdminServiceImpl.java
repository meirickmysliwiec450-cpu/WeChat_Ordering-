package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Admin;
import com.wechat.ordering.mapper.AdminMapper;
import com.wechat.ordering.service.AdminService;
import com.wechat.ordering.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Map<String, Object> login(String username, String password) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            throw new RuntimeException("账号不存在");
        }
        if (!admin.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        // 清除密码后返回
        admin.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("adminInfo", admin);
        // 生成JWT Token
        result.put("token", JwtUtil.generate(admin.getId(), admin.getUsername()));
        return result;
    }

    @Override
    public Admin getById(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin != null) {
            admin.setPassword(null);
        }
        return admin;
    }

    @Override
    public void update(Admin admin) {
        adminMapper.update(admin);
    }
}
