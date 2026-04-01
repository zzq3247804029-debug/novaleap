package com.novaleap.api.module.analytics.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

@Service
public class AnalyticsGeoService {

    private static final String KEY_PREFIX = "nova:analytics:";
    private static final String IP_GEO_CACHE_PREFIX = KEY_PREFIX + "geo:ip:";
    private static final String UNKNOWN_REGION = "未知地区";
    private static final String UNKNOWN_CITY = "未知城市";
    private static final String LOCAL_REGION = "本地网络";
    private static final String LOCAL_CITY = "内网访问";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AnalyticsGeoService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    public AnalyticsGeoInfo resolveGeoInfo(String ipAddress) {
        String ip = safeIp(ipAddress);
        if (!StringUtils.hasText(ip)) {
            return new AnalyticsGeoInfo(UNKNOWN_REGION, UNKNOWN_CITY);
        }
        if (isPrivateOrLocalIp(ip)) {
            return new AnalyticsGeoInfo(LOCAL_REGION, LOCAL_CITY);
        }

        String cacheKey = IP_GEO_CACHE_PREFIX + ip;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StringUtils.hasText(cached)) {
            String[] parts = cached.split("\\|", 2);
            if (parts.length == 2) {
                return new AnalyticsGeoInfo(parts[0], parts[1]);
            }
        }

        AnalyticsGeoInfo fetched = fetchGeoByIp(ip);
        redisTemplate.opsForValue().set(cacheKey, fetched.region() + "|" + fetched.city(), 7, TimeUnit.DAYS);
        return fetched;
    }

    private AnalyticsGeoInfo fetchGeoByIp(String ip) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://ipapi.co/" + ip + "/json/"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return new AnalyticsGeoInfo(UNKNOWN_REGION, UNKNOWN_CITY);
            }

            JsonNode node = objectMapper.readTree(response.body());
            if (node == null || node.path("error").asBoolean(false)) {
                return new AnalyticsGeoInfo(UNKNOWN_REGION, UNKNOWN_CITY);
            }

            String country = trimToDefault(node.path("country_name").asText(), "");
            String region = trimToDefault(node.path("region").asText(), UNKNOWN_REGION);
            String city = trimToDefault(node.path("city").asText(), UNKNOWN_CITY);
            String finalRegion = StringUtils.hasText(country) ? country + " / " + region : region;
            return new AnalyticsGeoInfo(finalRegion, city);
        } catch (Exception e) {
            return new AnalyticsGeoInfo(UNKNOWN_REGION, UNKNOWN_CITY);
        }
    }

    private String trimToDefault(String val, String fallback) {
        if (!StringUtils.hasText(val)) {
            return fallback;
        }
        return val.trim();
    }

    private String safeIp(String ipAddress) {
        if (!StringUtils.hasText(ipAddress)) {
            return "";
        }
        return ipAddress.trim();
    }

    private boolean isPrivateOrLocalIp(String ip) {
        if (!StringUtils.hasText(ip)) {
            return true;
        }
        String v = ip.trim().toLowerCase(Locale.ROOT);
        if ("127.0.0.1".equals(v) || "localhost".equals(v) || "::1".equals(v) || "0:0:0:0:0:0:0:1".equals(v)) {
            return true;
        }
        if (v.startsWith("10.") || v.startsWith("192.168.") || v.startsWith("169.254.") || v.startsWith("0.")) {
            return true;
        }
        if (v.startsWith("172.")) {
            String[] parts = v.split("\\.");
            if (parts.length > 1) {
                try {
                    int second = Integer.parseInt(parts[1]);
                    if (second >= 16 && second <= 31) {
                        return true;
                    }
                } catch (Exception ignore) {
                    return false;
                }
            }
        }
        return v.startsWith("fc") || v.startsWith("fd") || v.startsWith("fe80:");
    }
}