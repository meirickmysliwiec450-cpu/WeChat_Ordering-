package com.wechat.ordering.controller;

import com.wechat.ordering.entity.Admin;
import com.wechat.ordering.service.AdminService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/super")
public class SuperAdminController {

    @Autowired
    private AdminService adminService;

    /** 校验是否为超级管理员 */
    private boolean checkRole(HttpServletRequest request) {
        String role = (String) request.getAttribute("role");
        return "super_admin".equals(role);
    }

    @GetMapping("/admins")
    public Result<List<Admin>> listAdmins(HttpServletRequest request) {
        if (!checkRole(request)) return Result.error(403, "需要超级管理员权限");
        return Result.success(adminService.listAll());
    }

    @PostMapping("/register")
    public Result<?> register(HttpServletRequest request, @RequestBody Admin admin) {
        if (!checkRole(request)) return Result.error(403, "需要超级管理员权限");
        try {
            adminService.register(admin);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/toggle-status/{id}")
    public Result<?> toggleStatus(HttpServletRequest request, @PathVariable Long id, @RequestBody Map<String, Integer> params) {
        if (!checkRole(request)) return Result.error(403, "需要超级管理员权限");
        try {
            adminService.toggleStatus(id, params.get("status"));
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
