package com.novaleap.api.module.questionbank.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.novaleap.api.entity.QuestionCategory;
import com.novaleap.api.mapper.QuestionCategoryMapper;
import com.novaleap.api.module.system.catalog.QuestionCategoryCatalog;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class QuestionBankSupport {

    private final QuestionCategoryMapper questionCategoryMapper;

    public QuestionBankSupport(QuestionCategoryMapper questionCategoryMapper) {
        this.questionCategoryMapper = questionCategoryMapper;
    }

    public String resolveBankName(String requestedName, String originalFileName) {
        if (StringUtils.hasText(requestedName)) {
            return limitLength(requestedName.trim(), 120);
        }
        String fallback = StringUtils.hasText(originalFileName) ? originalFileName.trim() : "";
        if (!StringUtils.hasText(fallback)) {
            return "我的自定义题库";
        }
        int dotIndex = fallback.lastIndexOf('.');
        String baseName = dotIndex > 0 ? fallback.substring(0, dotIndex) : fallback;
        return limitLength(baseName.trim(), 120);
    }

    public String normalizeCategory(String category) {
        if (!StringUtils.hasText(category)) {
            return null;
        }
        String normalized = canonicalizeCategoryCode(category);
        if (!StringUtils.hasText(normalized) || normalized.length() > 64) {
            return null;
        }
        try {
            LambdaQueryWrapper<QuestionCategory> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(QuestionCategory::getCode, normalized)
                    .eq(QuestionCategory::getEnabled, 1);
            if (questionCategoryMapper.selectCount(wrapper) > 0) {
                return normalized;
            }
        } catch (Exception ignore) {
            // Ignore DB check errors and fallback to built-ins.
        }
        return QuestionCategoryCatalog.isBuiltin(normalized) ? normalized : null;
    }

    public String canonicalizeCategoryCode(String raw) {
        return QuestionCategoryCatalog.canonicalize(raw);
    }

    public Integer normalizeDifficulty(Integer difficulty) {
        if (difficulty == null || difficulty < 1 || difficulty > 3) {
            return null;
        }
        return difficulty;
    }

    public Integer normalizeBankStatus(Integer status) {
        if (status == null || status < 0 || status > 2) {
            return null;
        }
        return status;
    }

    public String normalizeRejectReason(String reason) {
        String trimmed = trim(reason);
        if (!StringUtils.hasText(trimmed)) {
            return "";
        }
        return limitLength(trimmed, 240);
    }

    public String buildPreviewText(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String preview = raw.replace("\r\n", "\n").replace('\r', '\n').replace('\n', ' ').trim();
        return preview.length() <= 160 ? preview : preview.substring(0, 160) + "...";
    }

    public String buildQuestionTags(String bankName) {
        return limitLength("custom-bank," + trim(bankName), 255);
    }

    public String buildOfficialQuestionTags(String bankName) {
        String normalizedName = trim(bankName);
        if (!StringUtils.hasText(normalizedName)) {
            normalizedName = "official";
        }
        return limitLength("official-bank," + normalizedName, 255);
    }

    public String limitLength(String value, int maxLength) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public String trim(String value) {
        return value == null ? null : value.trim();
    }
}
