package com.novaleap.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaleap.api.common.Result;
import com.novaleap.api.module.admin.question.dto.AdminQuestionSaveRequest;
import com.novaleap.api.module.admin.question.service.AdminQuestionApplicationService;
import com.novaleap.api.module.admin.question.vo.AdminQuestionVO;
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
@RequestMapping("/api/admin/questions")
public class AdminQuestionController {

    private final AdminQuestionApplicationService adminQuestionApplicationService;

    public AdminQuestionController(AdminQuestionApplicationService adminQuestionApplicationService) {
        this.adminQuestionApplicationService = adminQuestionApplicationService;
    }

    @GetMapping
    public Result<Page<AdminQuestionVO>> getQuestionList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "difficulty", required = false) Integer difficulty,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return Result.success(adminQuestionApplicationService.getQuestionList(page, size, category, difficulty, keyword));
    }

    @GetMapping("/{id}")
    public Result<AdminQuestionVO> getQuestionDetail(@PathVariable("id") Long id) {
        return Result.success(adminQuestionApplicationService.getQuestionDetail(id));
    }

    @PostMapping
    public Result<AdminQuestionVO> createQuestion(@RequestBody @Valid AdminQuestionSaveRequest request) {
        return Result.success(adminQuestionApplicationService.createQuestion(request));
    }

    @PutMapping("/{id}")
    public Result<AdminQuestionVO> updateQuestion(
            @PathVariable("id") Long id,
            @RequestBody @Valid AdminQuestionSaveRequest request
    ) {
        return Result.success(adminQuestionApplicationService.updateQuestion(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteQuestion(@PathVariable("id") Long id) {
        adminQuestionApplicationService.deleteQuestion(id);
        return Result.success();
    }
}
