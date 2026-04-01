package com.novaleap.api.controller;

import com.novaleap.api.common.Result;
import com.novaleap.api.module.admin.dashboard.service.AdminDashboardApplicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminDashboardController {

    private final AdminDashboardApplicationService adminDashboardApplicationService;

    public AdminDashboardController(AdminDashboardApplicationService adminDashboardApplicationService) {
        this.adminDashboardApplicationService = adminDashboardApplicationService;
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(adminDashboardApplicationService.getDashboard());
    }

    @GetMapping("/system-monitor")
    public Result<Map<String, Object>> getSystemMonitor() {
        return Result.success(adminDashboardApplicationService.getSystemMonitor());
    }

    @GetMapping("/visitor-records")
    public Result<Map<String, Object>> getVisitorRecords(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "actorType", required = false) String actorType
    ) {
        return Result.success(adminDashboardApplicationService.getVisitorRecords(page, size, keyword, actorType));
    }
}
