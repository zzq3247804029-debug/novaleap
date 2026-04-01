package com.novaleap.api.module.admin.question.support;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminQuestionCategorySupportTest {

    @Test
    void shouldCanonicalizeAliasCategoryCodes() {
        assertEquals("db", AdminQuestionCategorySupport.canonicalizeCategoryCode("database"));
        assertEquals("algo", AdminQuestionCategorySupport.canonicalizeCategoryCode("Algorithm"));
        assertEquals("system-design", AdminQuestionCategorySupport.canonicalizeCategoryCode("system sign"));
        assertEquals("arch", AdminQuestionCategorySupport.canonicalizeCategoryCode("architecture"));
    }

    @Test
    void shouldExpandCategoryFilterAliases() {
        List<String> codes = AdminQuestionCategorySupport.resolveFilterCodes("system-design");

        assertTrue(codes.contains("system-design"));
        assertTrue(codes.contains("system-sign"));
        assertTrue(codes.contains("system_sign"));
        assertTrue(codes.contains("system design"));
    }
}
