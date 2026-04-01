package com.novaleap.api.module.ai.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novaleap.api.module.ai.audit.AiCallAuditService;
import com.novaleap.api.service.AiLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
@Service
public class AiModelGateway {

    private static final String KEY_MODEL_USAGE_PREFIX = "nova:ai:usage:";
    private static final long MAX_MODEL_TOKEN_LIMIT = 500000;

    private final ChatClient chatClient;
    private final StringRedisTemplate redisTemplate;
    private final AiLimitService aiLimitService;
    private final ObjectMapper objectMapper;
    private final AiCallAuditService aiCallAuditService;
    private final String defaultModel;
    private final String fallbackModel;

    public AiModelGateway(
            ChatClient.Builder chatClientBuilder,
            StringRedisTemplate redisTemplate,
            AiLimitService aiLimitService,
            ObjectMapper objectMapper,
            AiCallAuditService aiCallAuditService,
            @Value("${spring.ai.openai.chat.options.model:LongCat-Flash-Lite}") String defaultModel,
            @Value("${spring.ai.openai.usage.fallback-model:LongCat-Flash-Chat}") String fallbackModel
    ) {
        this.chatClient = chatClientBuilder.build();
        this.redisTemplate = redisTemplate;
        this.aiLimitService = aiLimitService;
        this.objectMapper = objectMapper;
        this.aiCallAuditService = aiCallAuditService;
        this.defaultModel = defaultModel;
        this.fallbackModel = fallbackModel;
    }

    public SseEmitter streamStaticAnswer(String content) {
        SseEmitter emitter = new SseEmitter(120_000L);
        CompletableFuture.runAsync(() -> {
            try {
                String text = safe(content);
                for (String chunk : splitForSse(text, 48)) {
                    Map<String, String> payload = new LinkedHashMap<>();
                    payload.put("type", "delta");
                    payload.put("content", chunk);
                    emitter.send(SseEmitter.event().name("message").data(objectMapper.writeValueAsString(payload)));
                    Thread.sleep(22L);
                }

                Map<String, String> done = new LinkedHashMap<>();
                done.put("type", "done");
                emitter.send(SseEmitter.event().name("message").data(objectMapper.writeValueAsString(done)));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });
        return emitter;
    }

    public SseEmitter streamModelAnswer(
            String systemPrompt,
            String prompt,
            String fallback,
            Consumer<String> onCompleted,
            AiLimitService.AiModule module
    ) {
        return streamModelWithRetry(systemPrompt, prompt, fallback, onCompleted, resolveCurrentModel(), true, module);
    }

    public String callModel(String systemPrompt, String prompt, String fallback, AiLimitService.AiModule module) {
        try {
            String raw = callModelRaw(systemPrompt, prompt, module);
            return StringUtils.hasText(raw) ? raw.trim() : fallback;
        } catch (Exception e) {
            aiCallAuditService.recordFailure(resolveCurrentModel(), module, e.getMessage());
            log.warn("call model failed: {}", e.getMessage());
            return fallback;
        }
    }

    public String callModelRaw(String systemPrompt, String prompt, AiLimitService.AiModule module) {
        String model = resolveCurrentModel();
        try {
            return callModelExecution(systemPrompt, prompt, model, module);
        } catch (Exception e) {
            aiCallAuditService.recordFailure(model, module, e.getMessage());
            if (isQuotaExceededException(e) && !model.equals(fallbackModel)) {
                log.warn("Model {} quota exceeded, falling back to {}", model, fallbackModel);
                redisTemplate.opsForValue().set(KEY_MODEL_USAGE_PREFIX + model, String.valueOf(MAX_MODEL_TOKEN_LIMIT));
                return callModelExecution(systemPrompt, prompt, fallbackModel, module);
            }
            throw e;
        }
    }

    private SseEmitter streamModelWithRetry(
            String systemPrompt,
            String prompt,
            String fallback,
            Consumer<String> onCompleted,
            String model,
            boolean allowRetry,
            AiLimitService.AiModule module
    ) {
        SseEmitter emitter = new SseEmitter(120_000L);
        StringBuilder answerBuilder = new StringBuilder();
        AtomicBoolean finished = new AtomicBoolean(false);
        try {
            var request = chatClient.prompt();
            if (StringUtils.hasText(systemPrompt)) {
                request = request.system(systemPrompt);
            }
            request.user(u -> u.text(prompt))
                    .options(OpenAiChatOptions.builder()
                            .withModel(model)
                            .withMaxTokens(calculateMaxTokens(model, aiLimitService.getCurrentDegradeLevel(), module))
                            .build())
                    .stream()
                    .chatResponse()
                    .subscribe(
                            response -> handleStreamChunk(emitter, answerBuilder, model, module, finished, response),
                            error -> {
                                aiCallAuditService.recordFailure(model, module, error.getMessage());
                                log.warn("stream model {} failed: {}", model, error.getMessage());
                                if (allowRetry && isQuotaExceededException(error) && !model.equals(fallbackModel)) {
                                    redisTemplate.opsForValue().set(KEY_MODEL_USAGE_PREFIX + model, String.valueOf(MAX_MODEL_TOKEN_LIMIT));
                                    finishModelStream(emitter, answerBuilder, "正在切换备用模型，请稍后重试。", onCompleted, finished);
                                } else {
                                    finishModelStream(emitter, answerBuilder, fallback, onCompleted, finished);
                                }
                            },
                            () -> finishModelStream(emitter, answerBuilder, fallback, onCompleted, finished)
                    );
            emitter.onTimeout(() -> finishModelStream(emitter, answerBuilder, fallback, onCompleted, finished));
            return emitter;
        } catch (Exception e) {
            aiCallAuditService.recordFailure(model, module, e.getMessage());
            log.warn("stream is unavailable for {}, fallback to static: {}", model, e.getMessage());
            return null;
        }
    }

    private void handleStreamChunk(
            SseEmitter emitter,
            StringBuilder answerBuilder,
            String model,
            AiLimitService.AiModule module,
            AtomicBoolean finished,
            ChatResponse response
    ) {
        if (finished.get()) {
            return;
        }
        String chunk = response.getResult().getOutput().getContent();
        if (StringUtils.hasText(chunk)) {
            answerBuilder.append(chunk);
            sendDeltaSafely(emitter, chunk);
        }
        Usage usage = response.getMetadata().getUsage();
        if (usage != null) {
            recordModelUsage(model, module, usage);
        }
    }

    private void finishModelStream(
            SseEmitter emitter,
            StringBuilder answerBuilder,
            String fallback,
            Consumer<String> onCompleted,
            AtomicBoolean finished
    ) {
        if (!finished.compareAndSet(false, true)) {
            return;
        }
        String finalText = answerBuilder.length() == 0 ? safe(fallback) : answerBuilder.toString().trim();
        if (answerBuilder.length() == 0 && StringUtils.hasText(finalText)) {
            sendDeltaSafely(emitter, finalText);
        }
        sendDoneSafely(emitter);
        try {
            emitter.complete();
        } catch (Exception ignored) {
        }
        if (onCompleted != null) {
            try {
                onCompleted.accept(finalText);
            } catch (Exception e) {
                log.debug("post stream callback failed: {}", e.getMessage());
            }
        }
    }

    private void sendDeltaSafely(SseEmitter emitter, String chunk) {
        if (chunk == null || chunk.isEmpty()) {
            return;
        }
        int offset = 0;
        while (offset < chunk.length()) {
            int codePoint = chunk.codePointAt(offset);
            String part = new String(Character.toChars(codePoint));
            try {
                Map<String, String> payload = new LinkedHashMap<>();
                payload.put("type", "delta");
                payload.put("content", part);
                emitter.send(SseEmitter.event().name("message").data(objectMapper.writeValueAsString(payload)));
            } catch (Exception e) {
                log.debug("send delta failed: {}", e.getMessage());
                return;
            }
            offset += Character.charCount(codePoint);
        }
    }

    private void sendDoneSafely(SseEmitter emitter) {
        try {
            Map<String, String> done = new LinkedHashMap<>();
            done.put("type", "done");
            emitter.send(SseEmitter.event().name("message").data(objectMapper.writeValueAsString(done)));
        } catch (Exception e) {
            log.debug("send done failed: {}", e.getMessage());
        }
    }

    private List<String> splitForSse(String text, int chunkSize) {
        if (!StringUtils.hasText(text)) {
            return Collections.emptyList();
        }
        int size = Math.max(1, chunkSize);
        List<String> chunks = new ArrayList<>();
        int index = 0;
        while (index < text.length()) {
            int end = Math.min(text.length(), index + size);
            chunks.add(text.substring(index, end));
            index = end;
        }
        return chunks;
    }

    private String callModelExecution(String systemPrompt, String prompt, String model, AiLimitService.AiModule module) {
        var request = chatClient.prompt();
        if (StringUtils.hasText(systemPrompt)) {
            request = request.system(systemPrompt);
        }
        ChatResponse response = request.user(u -> u.text(prompt))
                .options(OpenAiChatOptions.builder()
                        .withModel(model)
                        .withMaxTokens(calculateMaxTokens(model, aiLimitService.getCurrentDegradeLevel(), module))
                        .build())
                .call()
                .chatResponse();

        String content = response.getResult().getOutput().getContent();
        recordModelUsage(model, module, response.getMetadata().getUsage());
        return content == null ? "" : content.trim();
    }

    private String resolveCurrentModel() {
        if (!isModelQuotaExceeded(defaultModel)) {
            return defaultModel;
        }
        return fallbackModel;
    }

    private boolean isModelQuotaExceeded(String model) {
        if (!StringUtils.hasText(model)) {
            return true;
        }
        String val = redisTemplate.opsForValue().get(KEY_MODEL_USAGE_PREFIX + model);
        if (val == null) {
            return false;
        }
        try {
            return Long.parseLong(val) >= MAX_MODEL_TOKEN_LIMIT;
        } catch (Exception e) {
            return false;
        }
    }

    private void recordModelUsage(String model, AiLimitService.AiModule module, Usage usage) {
        if (usage == null || !StringUtils.hasText(model)) {
            return;
        }
        long totalTokens = usage.getTotalTokens();
        if (totalTokens <= 0) {
            return;
        }
        redisTemplate.opsForValue().increment(KEY_MODEL_USAGE_PREFIX + model, totalTokens);
        aiLimitService.recordTokenUsage(totalTokens);
        aiCallAuditService.recordSuccess(model, module, totalTokens);
        log.info("Model {} consumed {} tokens.", model, totalTokens);
    }

    private int calculateMaxTokens(String model, int degradeLevel, AiLimitService.AiModule module) {
        int base = switch (module) {
            case RESUME -> 5000;
            case SOLVER -> 1200;
            case COACH -> 1000;
            case CHAT -> 800;
        };

        if (module == AiLimitService.AiModule.RESUME) {
            return base;
        }
        if (StringUtils.hasText(model) && model.toLowerCase().contains("flash")) {
            base = (int) (base * 1.25);
        }
        if (degradeLevel <= 0) {
            return base;
        }
        double factor = switch (degradeLevel) {
            case 1 -> 0.85;
            case 2 -> 0.7;
            case 3 -> 0.5;
            default -> 1.0;
        };
        return (int) (base * factor);
    }

    private boolean isQuotaExceededException(Throwable e) {
        if (e == null) {
            return false;
        }
        String msg = safe(e.getMessage()).toLowerCase();
        return msg.contains("quota") || msg.contains("balance") || msg.contains("insufficient") || msg.contains("429") || msg.contains("402");
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}