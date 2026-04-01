package com.novaleap.api.module.admin.question.assembler;

import com.novaleap.api.entity.Question;
import com.novaleap.api.module.admin.question.vo.AdminQuestionVO;

public final class AdminQuestionViewAssembler {

    private AdminQuestionViewAssembler() {
    }

    public static AdminQuestionVO toVO(Question question, String categoryName) {
        if (question == null) {
            return null;
        }
        AdminQuestionVO vo = new AdminQuestionVO();
        vo.setId(question.getId());
        vo.setTitle(question.getTitle());
        vo.setContent(question.getContent());
        vo.setStandardAnswer(question.getStandardAnswer());
        vo.setCategory(question.getCategory());
        vo.setCategoryName(categoryName);
        vo.setDifficulty(question.getDifficulty());
        vo.setTags(question.getTags());
        vo.setViewCount(question.getViewCount());
        vo.setStatus(question.getStatus());
        vo.setSourceType(question.getSourceType());
        vo.setCustomBankId(question.getCustomBankId());
        vo.setOwnerUserId(question.getOwnerUserId());
        vo.setCreatedAt(question.getCreatedAt());
        return vo;
    }
}
