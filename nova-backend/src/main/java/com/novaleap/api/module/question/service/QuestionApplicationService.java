package com.novaleap.api.module.question.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaleap.api.common.exception.NotFoundException;
import com.novaleap.api.entity.CustomQuestionBank;
import com.novaleap.api.entity.Question;
import com.novaleap.api.entity.QuestionCategory;
import com.novaleap.api.mapper.CustomQuestionBankMapper;
import com.novaleap.api.mapper.QuestionCategoryMapper;
import com.novaleap.api.mapper.QuestionMapper;
import com.novaleap.api.module.question.assembler.QuestionViewAssembler;
import com.novaleap.api.module.question.vo.QuestionAnswerVO;
import com.novaleap.api.module.question.vo.QuestionCategoryOptionVO;
import com.novaleap.api.module.question.vo.QuestionDetailVO;
import com.novaleap.api.module.question.vo.QuestionListItemVO;
import com.novaleap.api.module.question.vo.QuestionViewCountVO;
import com.novaleap.api.module.system.catalog.QuestionCategoryCatalog;
import com.novaleap.api.service.QuestionAccessSupport;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class QuestionApplicationService {

    private static final int MAX_PAGE_SIZE = 50;

    private final QuestionMapper questionMapper;
    private final CustomQuestionBankMapper customQuestionBankMapper;
    private final QuestionCategoryMapper questionCategoryMapper;
    private final QuestionAccessSupport questionAccessSupport;

    public QuestionApplicationService(
            QuestionMapper questionMapper,
            CustomQuestionBankMapper customQuestionBankMapper,
            QuestionCategoryMapper questionCategoryMapper,
            QuestionAccessSupport questionAccessSupport
    ) {
        this.questionMapper = questionMapper;
        this.customQuestionBankMapper = customQuestionBankMapper;
        this.questionCategoryMapper = questionCategoryMapper;
        this.questionAccessSupport = questionAccessSupport;
    }

    public Page<QuestionListItemVO> getQuestionList(
            Integer page,
            Integer size,
            String category,
            Integer difficulty,
            String keyword,
            Long bankId,
            Authentication authentication
    ) {
        Page<Question> pageParam = new Page<>(normalizePage(page), normalizeSize(size));
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getStatus, 1);

        if (bankId != null) {
            CustomQuestionBank bank = questionAccessSupport.resolveAccessibleBank(bankId, authentication);
            if (bank == null) {
                Page<QuestionListItemVO> emptyPage = new Page<>(pageParam.getCurrent(), pageParam.getSize(), 0);
                emptyPage.setRecords(Collections.emptyList());
                return emptyPage;
            }
            wrapper.eq(Question::getSourceType, QuestionAccessSupport.SOURCE_CUSTOM);
            wrapper.eq(Question::getCustomBankId, bankId);
        } else {
            wrapper.and(w -> w.eq(Question::getSourceType, QuestionAccessSupport.SOURCE_OFFICIAL)
                    .or()
                    .isNull(Question::getSourceType));
        }

        List<String> categoryFilterCodes = QuestionCategoryCatalog.expandFilterCodes(category);
        if (!categoryFilterCodes.isEmpty()) {
            if (categoryFilterCodes.size() == 1) {
                wrapper.eq(Question::getCategory, categoryFilterCodes.get(0));
            } else {
                wrapper.in(Question::getCategory, categoryFilterCodes);
            }
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Question::getTitle, keyword.trim());
        }
        wrapper.orderByDesc(Question::getCreatedAt);

        Page<Question> result = questionMapper.selectPage(pageParam, wrapper);
        Page<QuestionListItemVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream()
                .map(this::toQuestionListItemVO)
                .toList());
        return voPage;
    }

    public List<QuestionCategoryOptionVO> getQuestionCategoryList() {
        return loadQuestionCategoryOptions();
    }

    public QuestionDetailVO getQuestionDetail(Long id, Authentication authentication) {
        Question question = questionAccessSupport.resolveAccessibleQuestion(id, authentication);
        if (question == null) {
            throw new NotFoundException("题目不存在");
        }
        return toQuestionDetailVO(question);
    }

    public QuestionViewCountVO increaseQuestionView(Long id, Authentication authentication) {
        Question question = questionAccessSupport.resolveAccessibleQuestion(id, authentication);
        if (question == null) {
            throw new NotFoundException("题目不存在");
        }

        UpdateWrapper<Question> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", id)
                .eq("status", 1)
                .setSql("view_count = IFNULL(view_count, 0) + 1");
        int updated = questionMapper.update(null, wrapper);
        if (updated <= 0) {
            throw new NotFoundException("题目不存在");
        }

        Question refreshed = questionMapper.selectById(id);
        int nextCount = refreshed == null || refreshed.getViewCount() == null ? 0 : refreshed.getViewCount();
        return new QuestionViewCountVO(id, nextCount);
    }

    public QuestionDetailVO drawRandomQuestion(
            String category,
            Integer difficulty,
            Long bankId,
            Authentication authentication
    ) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Question::getStatus, 1);

        if (bankId != null) {
            CustomQuestionBank bank = questionAccessSupport.resolveAccessibleBank(bankId, authentication);
            if (bank == null) {
                throw new NotFoundException("当前筛选条件下暂无题目");
            }
            wrapper.eq(Question::getSourceType, QuestionAccessSupport.SOURCE_CUSTOM);
            wrapper.eq(Question::getCustomBankId, bankId);
        } else {
            wrapper.and(w -> w.eq(Question::getSourceType, QuestionAccessSupport.SOURCE_OFFICIAL)
                    .or()
                    .isNull(Question::getSourceType));
        }

        List<String> categoryFilterCodes = QuestionCategoryCatalog.expandFilterCodes(category);
        if (!categoryFilterCodes.isEmpty()) {
            if (categoryFilterCodes.size() == 1) {
                wrapper.eq(Question::getCategory, categoryFilterCodes.get(0));
            } else {
                wrapper.in(Question::getCategory, categoryFilterCodes);
            }
        }
        if (difficulty != null) {
            wrapper.eq(Question::getDifficulty, difficulty);
        }

        Long total = questionMapper.selectCount(wrapper);
        if (total == null || total <= 0) {
            throw new NotFoundException("当前筛选条件下暂无题目");
        }

        int offset = total <= 1 ? 0 : ThreadLocalRandom.current().nextInt(total.intValue());
        wrapper.orderByAsc(Question::getId).last("LIMIT " + offset + ",1");

        Question question = questionMapper.selectOne(wrapper);
        if (question == null) {
            throw new NotFoundException("当前筛选条件下暂无题目");
        }
        return toQuestionDetailVO(question);
    }

    public QuestionAnswerVO getQuestionAnswer(Long id, Authentication authentication) {
        Question question = questionAccessSupport.resolveAccessibleQuestion(id, authentication);
        if (question == null) {
            throw new NotFoundException("题目不存在");
        }

        String answer = question.getStandardAnswer();
        if (answer == null || answer.isBlank()) {
            answer = question.getContent();
        }
        answer = sanitizeOfficialAnswer(answer);

        return QuestionViewAssembler.toAnswerVO(
                question,
                answer == null ? "" : answer,
                resolveSourceLabel(question)
        );
    }

    private String resolveSourceLabel(Question question) {
        if (QuestionAccessSupport.SOURCE_CUSTOM.equalsIgnoreCase(safe(question.getSourceType()))) {
            CustomQuestionBank bank = question.getCustomBankId() == null
                    ? null
                    : customQuestionBankMapper.selectById(question.getCustomBankId());
            if (bank != null && bank.getName() != null && !bank.getName().isBlank()) {
                return "自定义题库 / " + bank.getName();
            }
            return "自定义题库";
        }
        return "官方题库";
    }

    private String sanitizeOfficialAnswer(String answer) {
        if (answer == null) {
            return "";
        }
        return answer
                .replaceFirst("^\\s*\\[(数据库标准答案|标准答案)]\\s*", "")
                .replaceFirst("^\\s*数据库标准答案[:：]?\\s*", "")
                .replaceFirst("^\\s*标准答案[:：]?\\s*", "")
                .trim();
    }

    private List<QuestionCategoryOptionVO> loadQuestionCategoryOptions() {
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
                String code = QuestionCategoryCatalog.canonicalize(row.getCode());
                String name = sanitizeCategoryName(code, row.getName());
                if (code.isBlank() || name.isBlank()) {
                    continue;
                }
                deduped.putIfAbsent(code, name);
            }
            if (!deduped.isEmpty()) {
                List<QuestionCategoryOptionVO> options = new ArrayList<>();
                for (Map.Entry<String, String> entry : deduped.entrySet()) {
                    options.add(new QuestionCategoryOptionVO(entry.getKey(), entry.getValue()));
                }
                return options;
            }
        }

        List<QuestionCategoryOptionVO> fallback = new ArrayList<>();
        for (String code : QuestionCategoryCatalog.builtinCodes()) {
            fallback.add(new QuestionCategoryOptionVO(code, QuestionCategoryCatalog.builtinLabel(code)));
        }
        return fallback;
    }

    private String sanitizeCategoryName(String code, String rawName) {
        String key = QuestionCategoryCatalog.canonicalize(code);
        if (key.isBlank()) {
            return "";
        }
        if (QuestionCategoryCatalog.isBuiltin(key)) {
            return QuestionCategoryCatalog.builtinLabel(key);
        }

        String name = safe(rawName);
        if (name.isBlank()) {
            return key;
        }

        boolean onlyQuestionMarks = name.chars().allMatch(ch -> ch == '?');
        if (name.contains("\uFFFD") || onlyQuestionMarks) {
            return key;
        }
        return name;
    }

    private QuestionListItemVO toQuestionListItemVO(Question question) {
        return QuestionViewAssembler.toListItemVO(
                question,
                QuestionCategoryCatalog.canonicalize(question.getCategory())
        );
    }

    private QuestionDetailVO toQuestionDetailVO(Question question) {
        return QuestionViewAssembler.toDetailVO(
                question,
                QuestionCategoryCatalog.canonicalize(question.getCategory())
        );
    }

    private int normalizePage(Integer value) {
        return value == null || value < 1 ? 1 : value;
    }

    private int normalizeSize(Integer value) {
        if (value == null || value < 1) {
            return 10;
        }
        return Math.min(value, MAX_PAGE_SIZE);
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
