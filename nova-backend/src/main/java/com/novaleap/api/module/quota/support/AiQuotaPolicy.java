package com.novaleap.api.module.quota.support;

import com.novaleap.api.service.AiLimitService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AiQuotaPolicy {

    private static final long THRESHOLD_WARNING = 700000;
    private static final long THRESHOLD_PROTECT = 850000;
    private static final long THRESHOLD_LIMIT = 900000;

    private static final Map<AiLimitService.AiModule, Integer> GUEST_LIMITS = Map.of(
            AiLimitService.AiModule.COACH, 2,
            AiLimitService.AiModule.SOLVER, 5,
            AiLimitService.AiModule.CHAT, 3,
            AiLimitService.AiModule.RESUME, 0
    );

    private static final Map<AiLimitService.AiModule, Integer> USER_LIMITS = Map.of(
            AiLimitService.AiModule.COACH, 8,
            AiLimitService.AiModule.SOLVER, 20,
            AiLimitService.AiModule.CHAT, 10,
            AiLimitService.AiModule.RESUME, 3
    );

    public int moduleLimit(String role, AiLimitService.AiModule module) {
        boolean guest = "GUEST".equalsIgnoreCase(role);
        return guest ? GUEST_LIMITS.getOrDefault(module, 0) : USER_LIMITS.getOrDefault(module, 0);
    }

    public int totalLimit(String role, int degradeLevel) {
        boolean guest = "GUEST".equalsIgnoreCase(role);
        if (!guest) {
            return 30;
        }
        return degradeLevel >= 1 ? 4 : 8;
    }

    public int cooldownSeconds(String role, AiLimitService.AiModule module) {
        boolean guest = "GUEST".equalsIgnoreCase(role);
        if (module == AiLimitService.AiModule.RESUME) {
            return 10;
        }
        return guest ? 5 : 3;
    }

    public int degradeLevel(long usage) {
        if (usage >= THRESHOLD_LIMIT) {
            return 3;
        }
        if (usage >= THRESHOLD_PROTECT) {
            return 2;
        }
        if (usage >= THRESHOLD_WARNING) {
            return 1;
        }
        return 0;
    }
}