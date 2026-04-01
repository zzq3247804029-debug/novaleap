package com.novaleap.api.module.ai.assembler;

import com.novaleap.api.module.ai.vo.AiCoachHistoryItemVO;

import java.util.List;
import java.util.Map;

public final class AiViewAssembler {

    private AiViewAssembler() {
    }

    public static List<AiCoachHistoryItemVO> toCoachHistory(List<Map<String, Object>> rows) {
        if (rows == null || rows.isEmpty()) {
            return List.of();
        }
        return rows.stream().map(AiViewAssembler::toCoachHistoryItem).toList();
    }

    public static AiCoachHistoryItemVO toCoachHistoryItem(Map<String, Object> row) {
        AiCoachHistoryItemVO vo = new AiCoachHistoryItemVO();
        vo.setRole(toStr(row.get("role")));
        vo.setContent(toStr(row.get("content")));
        vo.setMode(toStr(row.get("mode")));
        vo.setTopic(toStr(row.get("topic")));
        vo.setSessionId(toStr(row.get("sessionId")));
        vo.setTimestamp(toStr(row.get("timestamp")));
        return vo;
    }

    private static String toStr(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
