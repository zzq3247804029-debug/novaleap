package com.novaleap.api.module.admin.question.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.novaleap.api.entity.QuestionCategory;
import com.novaleap.api.mapper.QuestionCategoryMapper;
import com.novaleap.api.module.admin.question.vo.AdminQuestionCategoryVO;
import com.novaleap.api.module.system.catalog.QuestionCategoryCatalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AdminQuestionCategorySupport {

    private static final Map<String, String> BUILTIN_LABELS = Map.ofEntries(
            Map.entry("java", "Java 核心"),
            Map.entry("spring", "Spring 生态"),
            Map.entry("db", "数据存储"),
            Map.entry("redis", "Redis"),
            Map.entry("algo", "算法"),
            Map.entry("network", "计算机网络"),
            Map.entry("system-design", "系统设计"),
            Map.entry("arch", "架构设计"),
            Map.entry("linux", "Linux")
    );

    private AdminQuestionCategorySupport() {
    }

    public static List<AdminQuestionCategoryVO> loadOptions(QuestionCategoryMapper questionCategoryMapper) {
        List<QuestionCategory> dbRows;
        try {
            LambdaQueryWrapper<QuestionCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(QuestionCategory::getEnabled, 1)
                    .orderByAsc(QuestionCategory::getSortOrder)
                    .orderByAsc(QuestionCategory::getId);
            dbRows = questionCategoryMapper.selectList(wrapper);
        } catch (Exception ignore) {
            dbRows = Collections.emptyList();
        }

        if (dbRows != null && !dbRows.isEmpty()) {
            Map<String, String> deduped = new LinkedHashMap<>();
            for (QuestionCategory row : dbRows) {
                String code = canonicalizeCategoryCode(row.getCode());
                String name = sanitizeCategoryName(code, row.getName());
                if (isBlank(code) || isBlank(name)) {
                    continue;
                }
                deduped.putIfAbsent(code, name);
            }
            if (!deduped.isEmpty()) {
                List<AdminQuestionCategoryVO> options = new ArrayList<>();
                for (Map.Entry<String, String> entry : deduped.entrySet()) {
                    options.add(toVO(entry.getKey(), entry.getValue()));
                }
                return options;
            }
        }

        List<AdminQuestionCategoryVO> fallback = new ArrayList<>();
        for (String code : QuestionCategoryCatalog.builtinCodes()) {
            fallback.add(toVO(code, BUILTIN_LABELS.getOrDefault(code, code)));
        }
        return fallback;
    }

    public static String resolveCategoryName(String code, List<AdminQuestionCategoryVO> options) {
        String canonical = canonicalizeCategoryCode(code);
        if (isBlank(canonical)) {
            return "";
        }
        for (AdminQuestionCategoryVO option : options) {
            if (canonical.equals(option.getCode())) {
                return option.getName();
            }
        }
        return BUILTIN_LABELS.getOrDefault(canonical, canonical);
    }

    public static List<String> resolveFilterCodes(String category) {
        return QuestionCategoryCatalog.expandFilterCodes(category);
    }

    public static String normalizeCategory(String category, QuestionCategoryMapper questionCategoryMapper) {
        String canonical = canonicalizeCategoryCode(category);
        if (isBlank(canonical) || canonical.length() > 64) {
            return null;
        }
        if (QuestionCategoryCatalog.isBuiltin(canonical)) {
            return canonical;
        }
        try {
            LambdaQueryWrapper<QuestionCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(QuestionCategory::getCode, canonical)
                    .eq(QuestionCategory::getEnabled, 1);
            return questionCategoryMapper.selectCount(wrapper) > 0 ? canonical : null;
        } catch (Exception ignore) {
            return null;
        }
    }

    public static String canonicalizeCategoryCode(String raw) {
        return QuestionCategoryCatalog.canonicalize(raw);
    }

    public static String normalizeCategoryCode(String raw) {
        if (isBlank(raw)) {
            return "";
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-");
        normalized = normalized.replaceAll("[^a-z0-9\\-一-龥_]", "");
        if (normalized.length() > 64) {
            normalized = normalized.substring(0, 64);
        }
        return normalized;
    }

    private static String sanitizeCategoryName(String code, String rawName) {
        String key = canonicalizeCategoryCode(code);
        if (isBlank(key)) {
            return "";
        }
        String builtin = BUILTIN_LABELS.get(key);
        if (!isBlank(builtin)) {
            return builtin;
        }
        String name = trim(rawName);
        if (isBlank(name)) {
            return key;
        }
        boolean onlyQuestionMarks = name.chars().allMatch(ch -> ch == '?');
        if (name.contains("\uFFFD") || onlyQuestionMarks) {
            return key;
        }
        return name;
    }

    private static AdminQuestionCategoryVO toVO(String code, String name) {
        AdminQuestionCategoryVO vo = new AdminQuestionCategoryVO();
        vo.setCode(code);
        vo.setName(name);
        return vo;
    }

    private static String trim(String value) {
        return value == null ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
