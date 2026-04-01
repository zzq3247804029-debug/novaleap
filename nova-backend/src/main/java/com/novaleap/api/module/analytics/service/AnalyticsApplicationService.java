package com.novaleap.api.module.analytics.service;

import com.novaleap.api.dto.VisitTrackRequest;
import com.novaleap.api.module.system.security.CurrentUser;
import com.novaleap.api.module.system.security.CurrentUserService;
import com.novaleap.api.module.system.web.ClientRequestService;
import com.novaleap.api.service.AnalyticsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AnalyticsApplicationService {

    private final AnalyticsService analyticsService;
    private final CurrentUserService currentUserService;
    private final ClientRequestService clientRequestService;

    public AnalyticsApplicationService(
            AnalyticsService analyticsService,
            CurrentUserService currentUserService,
            ClientRequestService clientRequestService
    ) {
        this.analyticsService = analyticsService;
        this.currentUserService = currentUserService;
        this.clientRequestService = clientRequestService;
    }

    public void trackVisit(VisitTrackRequest request, Authentication authentication, HttpServletRequest httpRequest) {
        CurrentUser currentUser = currentUserService.current(authentication);

        String actorType = "guest";
        String uniqueIdentity = "visitor:" + request.getVisitorId();
        String actorName = uniqueIdentity;

        if (currentUser.isAuthenticated()) {
            if (currentUser.guest()) {
                uniqueIdentity = "guest:" + currentUser.safeUsername();
                actorName = currentUser.safeUsername();
            } else {
                actorType = "user";
                uniqueIdentity = "user:" + currentUser.safeUsername();
                actorName = currentUser.safeUsername();
            }
        }

        analyticsService.trackVisit(
                actorType,
                uniqueIdentity,
                request.getVisitorId(),
                request.getPath(),
                clientRequestService.resolveClientIp(httpRequest),
                actorName
        );
    }

    public Map<String, Object> getVisitorRecords(Integer page, Integer size, String keyword, String actorType) {
        return analyticsService.getVisitorRecords(page, size, keyword, actorType);
    }
}
