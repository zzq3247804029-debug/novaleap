package com.novaleap.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaleap.api.common.Result;
import com.novaleap.api.module.admin.user.dto.AdminUserSaveRequest;
import com.novaleap.api.module.admin.user.service.AdminUserApplicationService;
import com.novaleap.api.module.admin.user.vo.AdminUserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserApplicationService adminUserApplicationService;

    public AdminUserController(AdminUserApplicationService adminUserApplicationService) {
        this.adminUserApplicationService = adminUserApplicationService;
    }

    @GetMapping
    public Result<Page<AdminUserVO>> getUserList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role
    ) {
        return Result.success(adminUserApplicationService.getUserList(page, size, keyword, role));
    }

    @GetMapping("/{id}")
    public Result<AdminUserVO> getUserDetail(@PathVariable("id") Long id) {
        return Result.success(adminUserApplicationService.getUserDetail(id));
    }

    @PostMapping
    public Result<AdminUserVO> createUser(@RequestBody @Valid AdminUserSaveRequest request) {
        return Result.success(adminUserApplicationService.createUser(request));
    }

    @PutMapping("/{id}")
    public Result<AdminUserVO> updateUser(
            @PathVariable("id") Long id,
            @RequestBody @Valid AdminUserSaveRequest request
    ) {
        return Result.success(adminUserApplicationService.updateUser(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable("id") Long id) {
        adminUserApplicationService.deleteUser(id);
        return Result.success();
    }
}
