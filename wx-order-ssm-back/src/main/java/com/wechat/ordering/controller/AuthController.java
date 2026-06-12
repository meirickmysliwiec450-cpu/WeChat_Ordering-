package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Admin;
import com.wechat.ordering.service.AdminService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
public class AuthController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> params) {
        try {
            Map<String, Object> result = adminService.login(params.get("username"), params.get("password"));
            return Result.success(result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/info")
    public Result<Admin> info(@RequestParam Long adminId) {
        Admin admin = adminService.getById(adminId);
        if (admin == null) {
            return Result.error("管理员不存在");
        }
        return Result.success(admin);
    }
}
