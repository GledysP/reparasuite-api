package com.reparasuite.api.security;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RequestCorrelationFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER = "X-Request-Id";
  public static final String REQUEST_ID_ATTR = "requestId";
  private static final String MDC_KEY = "requestId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    String requestId = resolveRequestId(request);

    request.setAttribute(REQUEST_ID_ATTR, requestId);
    response.setHeader(REQUEST_ID_HEADER, requestId);
    MDC.put(MDC_KEY, requestId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }

  private String resolveRequestId(HttpServletRequest request) {
    String header = request.getHeader(REQUEST_ID_HEADER);
    if (header != null) {
      String trimmed = header.trim();
      if (!trimmed.isBlank() && trimmed.length() <= 100) {
        return trimmed;
      }
    }
    return UUID.randomUUID().toString();
  }
}