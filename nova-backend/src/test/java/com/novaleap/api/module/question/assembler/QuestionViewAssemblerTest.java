package com.novaleap.api.module.question.assembler;

import com.novaleap.api.entity.Question;
import com.novaleap.api.module.question.vo.QuestionAnswerVO;
import com.novaleap.api.module.question.vo.QuestionDetailVO;
import com.novaleap.api.module.question.vo.QuestionListItemVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class QuestionViewAssemblerTest {

    @Test
    void shouldNotExposeStandardAnswerInPublicViews() {
        Question question = new Question();
        question.setId(11L);
        question.setTitle("Explain JVM memory areas");
        question.setContent("Describe heap, stack, metaspace.");
        question.setStandardAnswer("Reference answer should stay private.");
        question.setDifficulty(2);
        question.setCategory("java");
        question.setTags("jvm,memory");
        question.setViewCount(23);
        question.setSourceType("OFFICIAL");

        QuestionListItemVO listItemVO = QuestionViewAssembler.toListItemVO(question, "java");
        QuestionDetailVO detailVO = QuestionViewAssembler.toDetailVO(question, "java");
        QuestionAnswerVO answerVO = QuestionViewAssembler.toAnswerVO(question, question.getStandardAnswer(), "STANDARD");

        assertEquals(question.getTitle(), listItemVO.getTitle());
        assertEquals(question.getTitle(), detailVO.getTitle());
        assertEquals(question.getStandardAnswer(), answerVO.getAnswer());
        assertFalse(detailVO.toString().contains("standardAnswer"));
        assertFalse(listItemVO.toString().contains("standardAnswer"));
    }
}
