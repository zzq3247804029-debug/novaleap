package com.novaleap.api.module.system.catalog;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuestionCategoryCatalogTest {

    @Test
    void shouldCanonicalizeAliases() {
        assertEquals("db", QuestionCategoryCatalog.canonicalize("database"));
        assertEquals("db", QuestionCategoryCatalog.canonicalize("数据库"));
        assertEquals("system-design", QuestionCategoryCatalog.canonicalize("system sign"));
        assertEquals("arch", QuestionCategoryCatalog.canonicalize("架构"));
    }

    @Test
    void shouldExpandBuiltinFilterCodes() {
        List<String> codes = QuestionCategoryCatalog.expandFilterCodes("db");
        assertTrue(codes.contains("db"));
        assertTrue(codes.contains("database"));
        assertTrue(codes.contains("数据库"));
    }
}
