package com.novaleap.api.module.ai.support;

import com.novaleap.api.entity.Question;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AiPromptFactoryTest {

    private final AiPromptFactory promptFactory = new AiPromptFactory();

    @Test
    void shouldBuildQuestionExplainPrompt() {
        Question question = new Question();
        question.setTitle("What is Redis cache avalanche?");
        question.setContent("Explain the scenario.");
        question.setStandardAnswer("Use TTL randomization.");

        String prompt = promptFactory.buildQuestionExplainPrompt(question);

        assertTrue(prompt.contains("What is Redis cache avalanche?"));
        assertTrue(prompt.contains("Use TTL randomization."));
    }

    @Test
    void shouldBuildWishEmotionPrompt() {
        String prompt = promptFactory.buildWishEmotionPrompt("I want to become a better backend engineer.");

        assertTrue(prompt.contains("happy, hopeful, confused, anxious"));
        assertTrue(prompt.contains("better backend engineer"));
    }
}
