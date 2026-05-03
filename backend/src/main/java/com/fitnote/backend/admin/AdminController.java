package com.fitnote.backend.admin;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/dashboard")
    public ApiResponse<Map<String, Object>> dashboard() {
        return ApiResponse.ok(adminService.getDashboard());
    }

    @GetMapping("/users")
    public ApiResponse<List<Map<String, Object>>> users() {
        return ApiResponse.ok(adminService.getUsers());
    }

    @GetMapping("/workouts")
    public ApiResponse<List<Map<String, Object>>> workouts() {
        return ApiResponse.ok(adminService.getWorkouts());
    }

    @GetMapping("/posts")
    public ApiResponse<List<Map<String, Object>>> posts(@RequestParam(defaultValue = "") String status) {
        return ApiResponse.ok(adminService.getPosts(status));
    }

    @PutMapping("/posts/{id}/audit")
    public ApiResponse<Map<String, Object>> audit(@PathVariable Long id,
                                                   @RequestParam String status,
                                                   @RequestParam(defaultValue = "") String reason) {
        return ApiResponse.ok(adminService.auditPost(id, status, reason, CurrentUser.id()));
    }

    @GetMapping("/plans")
    public ApiResponse<List<Map<String, Object>>> plans() {
        return ApiResponse.ok(adminService.getPlans());
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<Map<String, Object>>> auditLogs(@RequestParam(defaultValue = "") String status,
                                                             @RequestParam(defaultValue = "") String targetType,
                                                             @RequestParam(defaultValue = "30") Integer days,
                                                             @RequestParam(defaultValue = "100") Integer limit) {
        return ApiResponse.ok(adminService.getAuditLogs(status, targetType, days, limit));
    }
}
