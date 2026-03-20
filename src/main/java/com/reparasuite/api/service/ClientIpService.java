package com.reparasuite.api.service;

import org.springframework.stereotype.Service;

import com.reparasuite.api.config.ProxyProperties;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ClientIpService {

  private final ProxyProperties proxyProperties;

  public ClientIpService(ProxyProperties proxyProperties) {
    this.proxyProperties = proxyProperties;
  }

  public String resolve(HttpServletRequest request) {
    if (request == null) {
      return "unknown";
    }

    if (proxyProperties.isTrustForwardHeaders()) {
      String forwardedFor = request.getHeader("X-Forwarded-For");
      if (forwardedFor != null && !forwardedFor.isBlank()) {
        String[] parts = forwardedFor.split(",");
        if (parts.length > 0) {
          String first = parts[0].trim();
          if (!first.isBlank()) {
            return first;
          }
        }
      }

      String forwarded = request.getHeader("Forwarded");
      if (forwarded != null && !forwarded.isBlank()) {
        String parsed = extractForwardedFor(forwarded);
        if (parsed != null && !parsed.isBlank()) {
          return parsed;
        }
      }

      String realIp = request.getHeader("X-Real-IP");
      if (realIp != null && !realIp.isBlank()) {
        return realIp.trim();
      }
    }

    String remoteAddr = request.getRemoteAddr();
    return (remoteAddr == null || remoteAddr.isBlank()) ? "unknown" : remoteAddr;
  }

  private String extractForwardedFor(String forwardedHeader) {
    String[] segments = forwardedHeader.split(";");
    for (String segment : segments) {
      String s = segment.trim();
      if (s.regionMatches(true, 0, "for=", 0, 4)) {
        String value = s.substring(4).trim();
        value = stripQuotes(value);
        int comma = value.indexOf(',');
        if (comma >= 0) {
          value = value.substring(0, comma).trim();
        }
        return value;
      }
    }
    return null;
  }

  private String stripQuotes(String value) {
    if (value == null) return null;
    String v = value.trim();
    if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
      return v.substring(1, v.length() - 1);
    }
    return v;
  }
}