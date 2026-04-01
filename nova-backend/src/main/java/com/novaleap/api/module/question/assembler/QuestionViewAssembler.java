package com.novaleap.api.module.question.assembler;

import com.novaleap.api.entity.Question;
import com.novaleap.api.module.question.vo.QuestionAnswerVO;
import com.novaleap.api.module.question.vo.QuestionDetailVO;
import com.novaleap.api.module.question.vo.QuestionListItemVO;

public final class QuestionViewAssembler {

    private QuestionViewAssembler() {
    }

    public static QuestionListItemVO toListItemVO(Question source, String normalizedCategory) {
        if (source == null) {
            return null;
        }
        QuestionListItemVO target = new QuestionListItemVO();
        target.setId(source.getId());
        target.setTitle(source.getTitle());
        target.setDifficulty(source.getDifficulty());
        target.setCategory(normalizedCategory);
        target.setTags(source.getTags());
        target.setViewCount(source.getViewCount());
        target.setSourceType(source.getSourceType());
        return target;
    }

    public static QuestionDetailVO toDetailVO(Question source, String normalizedCategory) {
        if (source == null) {
            return null;
        }
        QuestionDetailVO target = new QuestionDetailVO();
        target.setId(source.getId());
        target.setTitle(source.getTitle());
        target.setContent(source.getContent());
        target.setDifficulty(source.getDifficulty());
        target.setCategory(normalizedCategory);
        target.setTags(source.getTags());
        target.setViewCount(source.getViewCount());
        target.setSourceType(source.getSourceType());
        return target;
    }

    public static QuestionAnswerVO toAnswerVO(Question source, String answer, String sourceLabel) {
        if (source == null) {
            return null;
        }
        QuestionAnswerVO target = new QuestionAnswerVO();
        target.setId(source.getId());
        target.setTitle(source.getTitle());
        target.setAnswer(answer);
        target.setSource(sourceLabel);
        return target;
    }
}
