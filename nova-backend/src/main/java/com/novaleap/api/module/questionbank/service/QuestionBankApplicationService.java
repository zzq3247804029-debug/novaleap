package com.novaleap.api.module.questionbank.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaleap.api.common.exception.ForbiddenException;
import com.novaleap.api.common.exception.NotFoundException;
import com.novaleap.api.entity.CustomQuestionBank;
import com.novaleap.api.entity.User;
import com.novaleap.api.mapper.CustomQuestionBankMapper;
import com.novaleap.api.module.questionbank.assembler.QuestionBankViewAssembler;
import com.novaleap.api.module.questionbank.dto.QuestionBankRenameRequest;
import com.novaleap.api.module.questionbank.support.QuestionBankSupport;
import com.novaleap.api.module.questionbank.vo.CustomQuestionBankVO;
import com.novaleap.api.module.system.security.CurrentUserService;
import com.novaleap.api.service.QuestionAccessSupport;
import com.novaleap.api.service.QuestionBankImportService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class QuestionBankApplicationService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;

    private final CustomQuestionBankMapper customQuestionBankMapper;
    private final QuestionBankImportService questionBankImportService;
    private final QuestionAccessSupport questionAccessSupport;
    private final QuestionBankSupport questionBankSupport;
    private final CurrentUserService currentUserService;

    public QuestionBankApplicationService(
            CustomQuestionBankMapper customQuestionBankMapper,
            QuestionBankImportService questionBankImportService,
            QuestionAccessSupport questionAccessSupport,
            QuestionBankSupport questionBankSupport,
            CurrentUserService currentUserService
    ) {
        this.customQuestionBankMapper = customQuestionBankMapper;
        this.questionBankImportService = questionBankImportService;
        this.questionAccessSupport = questionAccessSupport;
        this.questionBankSupport = questionBankSupport;
        this.currentUserService = currentUserService;
    }

    public Page<CustomQuestionBankVO> getMyBanks(Authentication authentication, Integer page, Integer size, Integer status) {
        User currentUser = currentUserService.requireDatabaseUser(authentication, "游客账号不能管理自定义题库");

        Page<CustomQuestionBank> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CustomQuestionBank> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomQuestionBank::getUserId, currentUser.getId());

        Integer normalizedStatus = questionBankSupport.normalizeBankStatus(status);
        if (normalizedStatus != null) {
            wrapper.eq(CustomQuestionBank::getStatus, normalizedStatus);
        }
        wrapper.orderByDesc(CustomQuestionBank::getUpdatedAt).orderByDesc(CustomQuestionBank::getCreatedAt);

        Page<CustomQuestionBank> result = customQuestionBankMapper.selectPage(pageParam, wrapper);
        Page<CustomQuestionBankVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(bank -> QuestionBankViewAssembler.toUserVO(bank, questionBankSupport))
                .toList());
        return voPage;
    }

    public CustomQuestionBankVO importQuestionBank(
            Authentication authentication,
            MultipartFile file,
            String name,
            String category,
            Integer difficulty
    ) throws IOException {
        User currentUser = currentUserService.requireDatabaseUser(authentication, "游客账号不能提交自定义题库");
        validateImportFile(file);

        QuestionBankImportService.ImportPayload payload = questionBankImportService.analyzeFile(file.getOriginalFilename(), file.getBytes());
        if (payload.questions().isEmpty()) {
            throw new IllegalArgumentException("未解析到有效题目，请上传符合模板格式的 TXT 文件");
        }

        String normalizedCategory = questionBankSupport.normalizeCategory(category);
        Integer normalizedDifficulty = questionBankSupport.normalizeDifficulty(difficulty);

        CustomQuestionBank bank = new CustomQuestionBank();
        bank.setUserId(currentUser.getId());
        bank.setName(questionBankSupport.resolveBankName(name, file.getOriginalFilename()));
        bank.setOriginalFileName(StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename().trim() : bank.getName());
        bank.setFileType(payload.fileType());
        bank.setRawContent(payload.rawText());
        bank.setCategory(normalizedCategory == null ? "java" : normalizedCategory);
        bank.setDifficulty(normalizedDifficulty == null ? 2 : normalizedDifficulty);
        bank.setStatus(QuestionAccessSupport.BANK_STATUS_PENDING);
        bank.setQuestionCount(payload.questions().size());
        bank.setImportedQuestionCount(0);
        bank.setRejectReason(null);
        bank.setAuditedAt(null);
        bank.setImportedAt(null);
        LocalDateTime now = LocalDateTime.now();
        bank.setCreatedAt(now);
        bank.setUpdatedAt(now);
        customQuestionBankMapper.insert(bank);

        return QuestionBankViewAssembler.toUserVO(bank, questionBankSupport);
    }

    public CustomQuestionBankVO renameQuestionBank(Long id, QuestionBankRenameRequest body, Authentication authentication) {
        User currentUser = currentUserService.requireDatabaseUser(authentication, "游客账号不能重命名自定义题库");

        CustomQuestionBank bank = customQuestionBankMapper.selectById(id);
        if (bank == null) {
            throw new NotFoundException("题库不存在");
        }
        if (!Objects.equals(currentUser.getId(), bank.getUserId()) && !questionAccessSupport.isAdmin(authentication)) {
            throw new ForbiddenException("无权重命名该题库");
        }

        bank.setName(questionBankSupport.limitLength(body.getName().trim(), 120));
        bank.setUpdatedAt(LocalDateTime.now());
        customQuestionBankMapper.updateById(bank);
        return QuestionBankViewAssembler.toUserVO(bank, questionBankSupport);
    }

    private void validateImportFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择题库文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小不能超过 5MB");
        }
    }
}
