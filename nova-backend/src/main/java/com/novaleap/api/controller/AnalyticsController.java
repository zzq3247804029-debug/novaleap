package com.novaleap.api.controller;

import com.novaleap.api.common.Result;
import com.novaleap.api.dto.VisitTrackRequest;
import com.novaleap.api.module.analytics.service.AnalyticsApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsApplicationService analyticsApplicationService;

    public AnalyticsController(AnalyticsApplicationService analyticsApplicationService) {
        this.analyticsApplicationService = analyticsApplicationService;
    }

    @PostMapping("/visit")
    public Result<Void> trackVisit(
            @RequestBody @Valid VisitTrackRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        analyticsApplicationService.trackVisit(request, authentication, httpRequest);
        return Result.success();
    }
}
