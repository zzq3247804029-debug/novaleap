package com.novaleap.api.service;

import java.util.Map;

/**
 * 全站 AI 限流与动态降级核心服务
 */
public interface AiLimitService {

    /**
     * 校验 AI 请求权限并增加计数
     * @param username 用户名 (若为游客则传入访客标识/IP)
     * @param role 用户角色 (GUEST / USER)
     * @param module 模块枚举 (COACH / SOLVER / RESUME / CHAT)
     * @return 鉴权结果及降级建议
     */
    LimitCheckResult checkLimit(String username, String role, AiModule module);

    /**
     * 记录 Token 消耗
     * @param tokens 消耗的 token 数量
     */
    void recordTokenUsage(long tokens);

    /**
     * 获取当前的动态降级级别 (0-正常, 1-预警, 2-保护, 3-极限保护)
     */
    int getCurrentDegradeLevel();

    enum AiModule {
        COACH, SOLVER, RESUME, CHAT
    }

    class LimitCheckResult {
        private final boolean allowed;
        private final String message;
        private final int degradeLevel;

        public LimitCheckResult(boolean allowed, String message, int degradeLevel) {
            this.allowed = allowed;
            this.message = message;
            this.degradeLevel = degradeLevel;
        }

        public boolean isAllowed() { return allowed; }
        public String getMessage() { return message; }
        public int getDegradeLevel() { return degradeLevel; }
    }
}
