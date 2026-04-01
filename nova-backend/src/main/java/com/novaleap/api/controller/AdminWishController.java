package com.novaleap.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaleap.api.common.Result;
import com.novaleap.api.module.admin.wish.dto.AdminWishSaveRequest;
import com.novaleap.api.module.admin.wish.dto.AdminWishStatusRequest;
import com.novaleap.api.module.admin.wish.service.AdminWishApplicationService;
import com.novaleap.api.module.admin.wish.vo.AdminWishQueueStatsVO;
import com.novaleap.api.module.admin.wish.vo.AdminWishVO;
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

import java.util.List;

@RestController
@RequestMapping("/api/admin/wishes")
public class AdminWishController {

    private final AdminWishApplicationService adminWishApplicationService;

    public AdminWishController(AdminWishApplicationService adminWishApplicationService) {
        this.adminWishApplicationService = adminWishApplicationService;
    }

    @GetMapping
    public Result<Page<AdminWishVO>> getWishList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        return Result.success(adminWishApplicationService.getWishList(page, size, status));
    }

    @GetMapping("/{id}")
    public Result<AdminWishVO> getWishDetail(@PathVariable("id") Long id) {
        return Result.success(adminWishApplicationService.getWishDetail(id));
    }

    @GetMapping("/queue/stats")
    public Result<AdminWishQueueStatsVO> getWishQueueStats() {
        return Result.success(adminWishApplicationService.getWishQueueStats());
    }

    @GetMapping("/dead-letter")
    public Result<List<AdminWishVO>> getDeadLetterWishes(
            @RequestParam(value = "size", defaultValue = "20") Integer size
    ) {
        return Result.success(adminWishApplicationService.getDeadLetterWishes(size));
    }

    @PostMapping
    public Result<AdminWishVO> createWish(@RequestBody @Valid AdminWishSaveRequest request) {
        return Result.success(adminWishApplicationService.createWish(request));
    }

    @PutMapping("/{id}")
    public Result<AdminWishVO> updateWish(
            @PathVariable("id") Long id,
            @RequestBody @Valid AdminWishSaveRequest request
    ) {
        return Result.success(adminWishApplicationService.updateWish(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteWish(@PathVariable("id") Long id) {
        adminWishApplicationService.deleteWish(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateWishStatus(
            @PathVariable("id") Long id,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestBody(required = false) @Valid AdminWishStatusRequest body
    ) {
        adminWishApplicationService.updateWishStatus(id, status, body);
        return Result.success();
    }

    @PostMapping("/{id}/retry")
    public Result<Void> retryWishReview(@PathVariable("id") Long id) {
        adminWishApplicationService.retryWishReview(id);
        return Result.success();
    }
}
