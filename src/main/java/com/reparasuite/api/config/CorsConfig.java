package com.reparasuite.api.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Value("${reparasuite.cors.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}")
  private String allowedOriginPatternsRaw;

  @Value("${reparasuite.cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS}")
  private String allowedMethodsRaw;

  @Value("${reparasuite.cors.allowed-headers:Authorization,Content-Type,X-Requested-With,Accept,Origin}")
  private String allowedHeadersRaw;

  @Value("${reparasuite.cors.exposed-headers:Authorization}")
  private String exposedHeadersRaw;

  @Value("${reparasuite.cors.allow-credentials:true}")
  private boolean allowCredentials;

  @Value("${reparasuite.cors.max-age:3600}")
  private long maxAge;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();

    cfg.setAllowedOriginPatterns(parseCsv(
        allowedOriginPatternsRaw,
        List.of("http://localhost:*", "http://127.0.0.1:*")
    ));

    cfg.setAllowedMethods(parseCsv(
        allowedMethodsRaw,
        List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    ));

    cfg.setAllowedHeaders(parseCsv(
        allowedHeadersRaw,
        List.of("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
    ));

    cfg.setExposedHeaders(parseCsv(
        exposedHeadersRaw,
        List.of("Authorization")
    ));

    cfg.setAllowCredentials(allowCredentials);
    cfg.setMaxAge(maxAge);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }

  private List<String> parseCsv(String raw, List<String> fallback) {
    if (!StringUtils.hasText(raw)) {
      return fallback;
    }

    List<String> values = Arrays.stream(raw.split(","))
        .map(String::trim)
        .filter(StringUtils::hasText)
        .toList();

    return values.isEmpty() ? fallback : values;
  }
}