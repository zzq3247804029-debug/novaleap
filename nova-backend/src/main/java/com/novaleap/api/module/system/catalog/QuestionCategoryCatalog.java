package com.novaleap.api.module.system.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class QuestionCategoryCatalog {

    private static final List<String> BUILTIN_CODES = List.of(
            "java", "spring", "db", "redis", "algo", "network", "system-design", "arch", "linux"
    );

    private static final Set<String> BUILTIN_CODE_SET = Set.copyOf(BUILTIN_CODES);

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

    private static final Map<String, String> ALIASES = Map.ofEntries(
            Map.entry("database", "db"),
            Map.entry("数据库", "db"),
            Map.entry("algorithm", "algo"),
            Map.entry("算法", "algo"),
            Map.entry("计算机网络", "network"),
            Map.entry("system-sign", "system-design"),
            Map.entry("system_sign", "system-design"),
            Map.entry("system-design", "system-design"),
            Map.entry("system design", "system-design"),
            Map.entry("系统设计", "system-design"),
            Map.entry("architecture", "arch"),
            Map.entry("架构", "arch"),
            Map.entry("架构设计", "arch")
    );

    private QuestionCategoryCatalog() {
    }

    public static List<String> builtinCodes() {
        return BUILTIN_CODES;
    }

    public static boolean isBuiltin(String code) {
        return BUILTIN_CODE_SET.contains(canonicalize(code));
    }

    public static String builtinLabel(String code) {
        String canonical = canonicalize(code);
        return BUILTIN_LABELS.getOrDefault(canonical, canonical);
    }

    public static String canonicalize(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String text = raw.trim();
        String compact = text.replace('_', '-').replaceAll("\\s+", "-");
        String lower = compact.toLowerCase(Locale.ROOT);
        String alias = ALIASES.get(lower);
        if (alias != null && !alias.isBlank()) {
            return alias;
        }
        alias = ALIASES.get(text);
        if (alias != null && !alias.isBlank()) {
            return alias;
        }
        return lower;
    }

    public static List<String> expandFilterCodes(String category) {
        String canonical = canonicalize(category);
        if (canonical.isBlank() || "all".equalsIgnoreCase(canonical)) {
            return Collections.emptyList();
        }
        List<String> codes = new ArrayList<>();
        codes.add(canonical);
        switch (canonical) {
            case "db" -> {
                codes.add("database");
                codes.add("数据库");
            }
            case "algo" -> {
                codes.add("algorithm");
                codes.add("算法");
            }
            case "network" -> codes.add("计算机网络");
            case "system-design" -> {
                codes.add("system-sign");
                codes.add("system_sign");
                codes.add("system design");
                codes.add("系统设计");
            }
            case "arch" -> {
                codes.add("architecture");
                codes.add("架构");
                codes.add("架构设计");
            }
            default -> {
            }
        }
        return codes.stream().distinct().toList();
    }
}
