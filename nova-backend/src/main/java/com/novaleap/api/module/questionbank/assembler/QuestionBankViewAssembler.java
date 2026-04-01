package com.novaleap.api.module.questionbank.assembler;

import com.novaleap.api.entity.CustomQuestionBank;
import com.novaleap.api.module.questionbank.support.QuestionBankSupport;
import com.novaleap.api.module.questionbank.vo.CustomQuestionBankVO;

public final class QuestionBankViewAssembler {

    private QuestionBankViewAssembler() {
    }

    public static CustomQuestionBankVO toUserVO(CustomQuestionBank bank, QuestionBankSupport support) {
        CustomQuestionBankVO vo = new CustomQuestionBankVO();
        vo.setId(bank.getId());
        vo.setUserId(bank.getUserId());
        vo.setName(bank.getName());
        vo.setOriginalFileName(bank.getOriginalFileName());
        vo.setFileType(bank.getFileType());
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
        return vo;
    }
}
