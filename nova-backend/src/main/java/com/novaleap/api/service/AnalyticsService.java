package com.novaleap.api.service;

import java.util.Map;

public interface AnalyticsService {

    /**
     * Track a single page visit event.
     */
    void trackVisit(
            String actorType,
            String uniqueIdentity,
            String visitorId,
            String path,
            String ipAddress,
            String actorName
    );

    /**
     * Get dashboard level visit stats.
     */
    Map<String, Object> getVisitStats();

    /**
     * Get de-duplicated visitor records list.
     */
    Map<String, Object> getVisitorRecords(Integer page, Integer size, String keyword, String actorType);
}
