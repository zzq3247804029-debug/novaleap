package com.novaleap.api.module.admin.question.assembler;

import com.novaleap.api.entity.Question;
import com.novaleap.api.module.admin.question.vo.AdminQuestionVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminQuestionViewAssemblerTest {

    @Test
    void shouldMapQuestionToAdminViewObject() {
        Question question = new Question();
        question.setId(101L);
        question.setTitle("Explain Redis persistence");
        question.setContent("Describe RDB and AOF.");
        question.setStandardAnswer("Compare trade-offs and production choices.");
        question.setCategory("redis");
        question.setDifficulty(2);
        question.setTags("redis,persistence");
        question.setViewCount(12);
        question.setStatus(1);
        question.setSourceType("OFFICIAL");
        question.setCustomBankId(9L);
        question.setOwnerUserId(7L);
        question.setCreatedAt(LocalDateTime.of(2026, 3, 28, 10, 0));

        AdminQuestionVO vo = AdminQuestionViewAssembler.toVO(question, "Redis");

        assertEquals(question.getId(), vo.getId());
        assertEquals(question.getTitle(), vo.getTitle());
        assertEquals(question.getContent(), vo.getContent());
        assertEquals(question.getStandardAnswer(), vo.getStandardAnswer());
        assertEquals("redis", vo.getCategory());
        assertEquals("Redis", vo.getCategoryName());
        assertEquals(question.getDifficulty(), vo.getDifficulty());
        assertEquals(question.getTags(), vo.getTags());
        assertEquals(question.getViewCount(), vo.getViewCount());
        assertEquals(question.getStatus(), vo.getStatus());
        assertEquals(question.getSourceType(), vo.getSourceType());
        assertEquals(question.getCustomBankId(), vo.getCustomBankId());
        assertEquals(question.getOwnerUserId(), vo.getOwnerUserId());
        assertEquals(question.getCreatedAt(), vo.getCreatedAt());
    }
}
