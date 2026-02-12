package com.reparasuite.api.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();

    // Orígenes del navegador (host) cuando estás en Docker:
    // - Front: http://localhost:4200
    // - (opcional) si accedes por 127.0.0.1
    cfg.setAllowedOriginPatterns(List.of(
        "http://localhost:*",
        "http://127.0.0.1:*"
    ));

    cfg.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));

    // Importante: deja que el navegador pida Authorization/Content-Type/etc
    cfg.setAllowedHeaders(List.of(
        "Authorization",
        "Content-Type",
        "X-Requested-With",
        "Accept",
        "Origin"
    ));

    // Si devuelves Authorization en respuesta (JWT, etc.)
    cfg.setExposedHeaders(List.of("Authorization"));

    // Si usas cookies / withCredentials
    cfg.setAllowCredentials(true);

    // (opcional) cache del preflight
    cfg.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
  }
}
