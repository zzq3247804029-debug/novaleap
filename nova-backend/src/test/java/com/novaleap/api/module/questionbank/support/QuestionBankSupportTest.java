package com.novaleap.api.module.questionbank.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class QuestionBankSupportTest {

    private final QuestionBankSupport support = new QuestionBankSupport(null);

    @Test
    void shouldResolveFriendlyBankNameFromFilename() {
        assertEquals("backend-interview", support.resolveBankName("", "backend-interview.txt"));
        assertEquals("我的自定义题库", support.resolveBankName("", ""));
    }

    @Test
    void shouldNormalizeBuiltinCategoryAlias() {
        assertEquals("db", support.normalizeCategory("database"));
        assertEquals("system-design", support.canonicalizeCategoryCode("system design"));
        assertNull(support.normalizeDifficulty(5));
    }
}
