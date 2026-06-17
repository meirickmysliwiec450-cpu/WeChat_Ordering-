package com.wechat.ordering.service.impl;

import com.wechat.ordering.entity.Admin;
import com.wechat.ordering.mapper.AdminMapper;
import com.wechat.ordering.service.AdminService;
import com.wechat.ordering.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public Map<String, Object> login(String username, String password) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) throw new RuntimeException("账号不存在");
        if (admin.getStatus() != null && admin.getStatus() == 0) throw new RuntimeException("账号已被禁用");
        if (!admin.getPassword().equals(password)) throw new RuntimeException("密码错误");

        // 清除密码后返回
        admin.setPassword(null);
        Map<String, Object> result = new HashMap<>();
        result.put("adminInfo", admin);
        String role = admin.getRole() != null ? admin.getRole() : "admin";
        result.put("token", JwtUtil.generateWithRole(admin.getId(), admin.getUsername(), role));
        return result;
    }

    @Override
    public Admin getById(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin != null) admin.setPassword(null);
        return admin;
    }

    @Override
    public void update(Admin admin) {
        adminMapper.update(admin);
    }

    @Override
    public List<Admin> listAll() {
        return adminMapper.selectAll().stream().peek(a -> a.setPassword(null)).collect(Collectors.toList());
    }

    @Override
    public void register(Admin admin) {
        Admin exist = adminMapper.selectByUsername(admin.getUsername());
        if (exist != null) throw new RuntimeException("用户名已存在");
        admin.setRole("admin");
        admin.setStatus(1);
        admin.setCreateTime(LocalDateTime.now());
        adminMapper.insert(admin);
    }

    @Override
    public void toggleStatus(Long id, Integer status) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) throw new RuntimeException("管理员不存在");
        if ("super_admin".equals(admin.getRole())) throw new RuntimeException("不能禁用超级管理员");
        admin.setStatus(status);
        adminMapper.update(admin);
    }
}
