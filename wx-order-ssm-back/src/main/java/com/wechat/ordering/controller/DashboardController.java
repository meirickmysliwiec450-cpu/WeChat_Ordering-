package com.wechat.ordering.controller;

import com.wechat.ordering.service.DashboardService;
import com.wechat.ordering.util.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(dashboardService.getStats());
    }
}
