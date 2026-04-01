package com.novaleap.api.module.questionbank.assembler;

import com.novaleap.api.entity.CustomQuestionBank;
import com.novaleap.api.module.questionbank.support.QuestionBankSupport;
import com.novaleap.api.module.questionbank.vo.CustomQuestionBankVO;
import com.novaleap.api.mapper.QuestionCategoryMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QuestionBankViewAssemblerTest {

    @Test
    void shouldBuildUserViewWithoutRawContentField() {
        QuestionBankSupport support = new QuestionBankSupport((QuestionCategoryMapper) null);
        CustomQuestionBank bank = new CustomQuestionBank();
        bank.setId(1L);
        bank.setUserId(2L);
        bank.setName("My Bank");
        bank.setOriginalFileName("bank.txt");
        bank.setFileType("txt");
        bank.setCategory("database");
        bank.setDifficulty(2);
        bank.setStatus(1);
        bank.setQuestionCount(10);
        bank.setImportedQuestionCount(10);
        bank.setRejectReason("none");
        bank.setRawContent("hidden");

        CustomQuestionBankVO vo = QuestionBankViewAssembler.toUserVO(bank, support);

        assertEquals(1L, vo.getId());
        assertEquals("db", vo.getCategory());
        assertEquals(10, vo.getImportedQuestionCount());
        assertNull(findRawContent(vo));
    }

    private String findRawContent(CustomQuestionBankVO vo) {
        try {
            return (String) vo.getClass().getDeclaredField("rawContent").get(vo);
        } catch (Exception ignore) {
            return null;
        }
    }
}
