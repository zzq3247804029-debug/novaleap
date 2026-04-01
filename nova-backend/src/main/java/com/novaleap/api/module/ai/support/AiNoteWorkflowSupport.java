package com.novaleap.api.module.ai.support;

import com.novaleap.api.service.AiLimitService;
import com.novaleap.api.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AiNoteWorkflowSupport {

    private final AiLimitService aiLimitService;
    private final AiContentSupport aiContentSupport;
    private final AiPromptFactory aiPromptFactory;
    private final AiModelGateway aiModelGateway;

    public AiNoteWorkflowSupport(
            AiLimitService aiLimitService,
            AiContentSupport aiContentSupport,
            AiPromptFactory aiPromptFactory,
            AiModelGateway aiModelGateway
    ) {
        this.aiLimitService = aiLimitService;
        this.aiContentSupport = aiContentSupport;
        this.aiPromptFactory = aiPromptFactory;
        this.aiModelGateway = aiModelGateway;
    }

    public List<String> summarizeNote(String username, String role, String title, String noteContent, boolean hasAiCapability) {
        String cleanContent = safe(noteContent);
        if (cleanContent.isBlank()) {
            return aiContentSupport.defaultNoteSummaryPoints();
        }
        if (!hasAiCapability) {
            return aiContentSupport.summarizeByRule(cleanContent);
        }

        AiLimitService.LimitCheckResult limit = aiLimitService.checkLimit(username, role, AiLimitService.AiModule.CHAT);
        if (!limit.isAllowed()) {
            log.info("AI summarize limit reached for {}: {}", username, limit.getMessage());
            return aiContentSupport.summarizeByRule(cleanContent);
        }

        String prompt = aiPromptFactory.buildNoteSummaryPrompt(title, cleanContent);
        try {
            String raw = aiModelGateway.callModelRaw("", prompt, AiLimitService.AiModule.CHAT);
            List<String> parsed = aiContentSupport.parseSummaryPoints(raw);
            if (!parsed.isEmpty()) {
                while (parsed.size() < 3) {
                    parsed.add(aiContentSupport.defaultNoteSummaryPoints().get(parsed.size()));
                }
                return parsed.size() > 3 ? parsed.subList(0, 3) : parsed;
            }
        } catch (Exception e) {
            log.warn("AI summarize failed, fallback to rule summary: {}", e.getMessage());
        }
        return aiContentSupport.summarizeByRule(cleanContent);
    }

    public AiService.NoteModerationResult moderateNote(String title, String noteContent, boolean hasAiCapability) {
        String cleanTitle = safe(title);
        String cleanContent = safe(noteContent);
        String fullText = (cleanTitle + "\n" + cleanContent).trim();
        if (fullText.isBlank()) {
            return new AiService.NoteModerationResult(false, "Content is empty.", "RULES");
        }

        List<String> hitWords = aiContentSupport.matchBannedWords(fullText);
        if (!hitWords.isEmpty()) {
            return new AiService.NoteModerationResult(false, aiContentSupport.clipReason("Blocked words: " + String.join(", ", hitWords)), "RULES");
        }

        if (!hasAiCapability) {
            return new AiService.NoteModerationResult(true, "Rules-only moderation completed, manual review recommended.", "RULES_ONLY");
        }

        AiLimitService.LimitCheckResult limit = aiLimitService.checkLimit("SYSTEM_DEFAULT", "USER", AiLimitService.AiModule.CHAT);
        if (!limit.isAllowed()) {
            log.info("AI moderation limit reached: {}", limit.getMessage());
            return new AiService.NoteModerationResult(true, "Rate limited, fallback to rules-only moderation.", "RULES_ONLY");
        }

        String prompt = aiPromptFactory.buildNoteModerationPrompt(cleanTitle, cleanContent);
        try {
            String raw = aiModelGateway.callModelRaw("", prompt, AiLimitService.AiModule.CHAT);
            AiService.NoteModerationResult parsed = aiContentSupport.parseModerationResult(raw);
            if (parsed != null) {
                return parsed;
            }
            return new AiService.NoteModerationResult(true, "AI result is not parseable, manual review recommended.", "AI_FALLBACK");
        } catch (Exception e) {
            log.warn("AI moderation failed, fallback to manual audit: {}", e.getMessage());
            return new AiService.NoteModerationResult(true, "AI moderation failed, manual review recommended.", "AI_FALLBACK");
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}