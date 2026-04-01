package com.novaleap.api.service;

import com.novaleap.api.entity.CustomQuestionBank;
import com.novaleap.api.entity.User;
import com.novaleap.api.mapper.CustomQuestionBankMapper;
import com.novaleap.api.mapper.QuestionMapper;
import com.novaleap.api.mapper.UserMapper;
import com.novaleap.api.module.admin.questionbank.dto.AdminQuestionBankAuditRequest;
import com.novaleap.api.module.admin.questionbank.vo.AdminQuestionBankVO;
import com.novaleap.api.module.questionbank.support.QuestionBankSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminQuestionBankAuditServiceTest {

    @Mock
    private CustomQuestionBankMapper customQuestionBankMapper;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private QuestionBankImportService questionBankImportService;

    @Mock
    private QuestionBankSupport questionBankSupport;

    @InjectMocks
    private AdminQuestionBankAuditService adminQuestionBankAuditService;

    @Test
    void shouldImportQuestionsWhenApprovingPendingBank() {
        CustomQuestionBank bank = new CustomQuestionBank();
        bank.setId(1L);
        bank.setUserId(7L);
        bank.setName("bank-a");
        bank.setRawContent("raw");
        bank.setCategory("java");
        bank.setDifficulty(2);
        bank.setStatus(QuestionAccessSupport.BANK_STATUS_PENDING);

        AdminQuestionBankAuditRequest request = new AdminQuestionBankAuditRequest();
        request.setStatus(QuestionAccessSupport.BANK_STATUS_APPROVED);

        User owner = new User();
        owner.setId(7L);
        owner.setUsername("alice");
        owner.setNickname("Alice");

        when(customQuestionBankMapper.selectById(1L)).thenReturn(bank);
        when(questionBankSupport.normalizeBankStatus(QuestionAccessSupport.BANK_STATUS_APPROVED))
                .thenReturn(QuestionAccessSupport.BANK_STATUS_APPROVED);
        when(questionBankSupport.normalizeCategory("java")).thenReturn("java");
        when(questionBankSupport.normalizeDifficulty(2)).thenReturn(2);
        when(questionBankImportService.parseQuestions("raw")).thenReturn(List.of(
                new QuestionBankImportService.QuestionDraft("q1", "c1", "a1"),
                new QuestionBankImportService.QuestionDraft("q2", "c2", "a2")
        ));
        when(questionBankSupport.buildQuestionTags("bank-a")).thenReturn("tag");
        when(userMapper.selectBatchIds(List.of(7L))).thenReturn(List.of(owner));
        when(questionBankSupport.canonicalizeCategoryCode("java")).thenReturn("java");
        when(questionBankSupport.buildPreviewText("raw")).thenReturn("raw");

        AdminQuestionBankVO result = adminQuestionBankAuditService.auditQuestionBank(1L, request);

        verify(questionMapper, times(2)).insert(any());
        verify(customQuestionBankMapper).updateById(bank);
        assertEquals(2, bank.getImportedQuestionCount());
        assertEquals(2, bank.getQuestionCount());
        assertNotNull(bank.getImportedAt());
        assertEquals("alice", result.getOwnerUsername());
        assertEquals("raw", result.getPreviewText());

        ArgumentCaptor<CustomQuestionBank> captor = ArgumentCaptor.forClass(CustomQuestionBank.class);
        verify(customQuestionBankMapper).updateById(captor.capture());
        assertEquals(QuestionAccessSupport.BANK_STATUS_APPROVED, captor.getValue().getStatus());
    }
}
