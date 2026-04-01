package com.novaleap.api.module.ai.assembler;

import com.novaleap.api.module.ai.vo.AiCoachHistoryItemVO;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AiViewAssemblerTest {

    @Test
    void shouldConvertHistoryMapToTypedVo() {
        List<AiCoachHistoryItemVO> history = AiViewAssembler.toCoachHistory(List.of(
                Map.of(
                        "role", "assistant",
                        "content", "hello",
                        "mode", "coach",
                        "topic", "java",
                        "sessionId", "sess-1",
                        "timestamp", "2026-03-29T00:00:00"
                )
        ));

        assertEquals(1, history.size());
        assertEquals("assistant", history.get(0).getRole());
        assertEquals("hello", history.get(0).getContent());
        assertEquals("sess-1", history.get(0).getSessionId());
    }
}
