package com.novaleap.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaleap.api.common.Result;
import com.novaleap.api.module.questionbank.dto.QuestionBankRenameRequest;
import com.novaleap.api.module.questionbank.service.QuestionBankApplicationService;
import com.novaleap.api.module.questionbank.vo.CustomQuestionBankVO;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/api/question-banks")
public class QuestionBankController {

    private final QuestionBankApplicationService questionBankApplicationService;

    public QuestionBankController(QuestionBankApplicationService questionBankApplicationService) {
        this.questionBankApplicationService = questionBankApplicationService;
    }

    @GetMapping("/mine")
    public Result<Page<CustomQuestionBankVO>> getMyBanks(
            Authentication authentication,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "30") Integer size,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        return Result.success(questionBankApplicationService.getMyBanks(authentication, page, size, status));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<CustomQuestionBankVO> importQuestionBank(
            Authentication authentication,
            @RequestPart(value = "file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "difficulty", required = false) Integer difficulty
    ) throws IOException {
        return Result.success(questionBankApplicationService.importQuestionBank(authentication, file, name, category, difficulty));
    }

    @PutMapping("/{id}")
    public Result<CustomQuestionBankVO> renameQuestionBank(
            @PathVariable(value = "id") Long id,
            @RequestBody @Valid QuestionBankRenameRequest body,
            Authentication authentication
    ) {
        return Result.success(questionBankApplicationService.renameQuestionBank(id, body, authentication));
    }
}
