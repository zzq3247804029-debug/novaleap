package com.novaleap.api.module.admin.questionbank.assembler;

import com.novaleap.api.entity.CustomQuestionBank;
import com.novaleap.api.module.admin.questionbank.vo.AdminQuestionBankVO;
import com.novaleap.api.module.questionbank.support.QuestionBankSupport;

public final class AdminQuestionBankViewAssembler {

    private AdminQuestionBankViewAssembler() {
    }

    public static AdminQuestionBankVO toVO(CustomQuestionBank bank, QuestionBankSupport support) {
        AdminQuestionBankVO vo = new AdminQuestionBankVO();
        vo.setId(bank.getId());
        vo.setUserId(bank.getUserId());
        vo.setName(bank.getName());
        vo.setOriginalFileName(bank.getOriginalFileName());
        vo.setFileType(bank.getFileType());
        vo.setRawContent(bank.getRawContent());
        vo.setCategory(support.canonicalizeCategoryCode(bank.getCategory()));
        vo.setDifficulty(bank.getDifficulty());
        vo.setStatus(bank.getStatus());
        vo.setQuestionCount(bank.getQuestionCount());
        vo.setImportedQuestionCount(bank.getImportedQuestionCount());
        vo.setRejectReason(bank.getRejectReason());
        vo.setAuditedAt(bank.getAuditedAt());
        vo.setImportedAt(bank.getImportedAt());
        vo.setCreatedAt(bank.getCreatedAt());
        vo.setUpdatedAt(bank.getUpdatedAt());
        vo.setOwnerUsername(bank.getOwnerUsername());
        vo.setOwnerNickname(bank.getOwnerNickname());
        vo.setPreviewText(support.buildPreviewText(bank.getRawContent()));
        return vo;
    }
}
