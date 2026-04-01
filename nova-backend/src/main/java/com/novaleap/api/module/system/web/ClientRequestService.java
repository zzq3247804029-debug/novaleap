package com.novaleap.api.module.system.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ClientRequestService {

    public String resolveClientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }

        String[] headerKeys = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String key : headerKeys) {
            String value = request.getHeader(key);
            String parsed = firstIp(value);
            if (isUsableIp(parsed)) {
                return parsed;
            }
        }

        String remoteAddr = request.getRemoteAddr();
        if ("0:0:0:0:0:0:0:1".equals(remoteAddr) || "::1".equals(remoteAddr)) {
            return "127.0.0.1";
        }
        return remoteAddr == null ? "" : remoteAddr;
    }

    private String firstIp(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        String[] parts = raw.split(",");
        if (parts.length == 0) {
            return "";
        }
        return parts[0].trim();
    }

    private boolean isUsableIp(String ip) {
        return StringUtils.hasText(ip) && !"unknown".equalsIgnoreCase(ip);
    }
}
