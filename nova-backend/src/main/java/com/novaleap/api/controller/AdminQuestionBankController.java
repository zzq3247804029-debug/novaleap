package com.novaleap.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaleap.api.common.Result;
import com.novaleap.api.module.admin.questionbank.dto.AdminQuestionBankAuditRequest;
import com.novaleap.api.module.admin.questionbank.vo.AdminOfficialQuestionImportVO;
import com.novaleap.api.module.admin.questionbank.vo.AdminQuestionBankVO;
import com.novaleap.api.service.AdminQuestionBankAuditService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/question-banks")
public class AdminQuestionBankController {

    private final AdminQuestionBankAuditService adminQuestionBankAuditService;

    public AdminQuestionBankController(AdminQuestionBankAuditService adminQuestionBankAuditService) {
        this.adminQuestionBankAuditService = adminQuestionBankAuditService;
    }

    @GetMapping
    public Result<Page<AdminQuestionBankVO>> getQuestionBankList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "keyword", required = false) String keyword
    ) {
        return Result.success(adminQuestionBankAuditService.getQuestionBankList(page, size, status, keyword));
    }

    @PostMapping(value = "/official/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AdminOfficialQuestionImportVO> importOfficialQuestionBank(
            @RequestPart(value = "file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "difficulty", required = false) Integer difficulty
    ) throws IOException {
        return Result.success(adminQuestionBankAuditService.importOfficialQuestionBank(file, name, category, difficulty));
    }

    @PutMapping("/{id}/audit")
    public Result<AdminQuestionBankVO> auditQuestionBank(
            @PathVariable(value = "id") Long id,
            @RequestBody @Valid AdminQuestionBankAuditRequest body
    ) {
        return Result.success(adminQuestionBankAuditService.auditQuestionBank(id, body));
    }
}
