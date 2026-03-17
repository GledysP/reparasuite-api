package com.reparasuite.api.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

class CorsIntegrationTest {

  @Test
  void debeConfigurarCorsConOrigenesMetodosYHeaders() {
    CorsConfig corsConfig = new CorsConfig();
    CorsConfigurationSource source = corsConfig.corsConfigurationSource();

    assertNotNull(source);
    assertTrue(source instanceof UrlBasedCorsConfigurationSource);

    UrlBasedCorsConfigurationSource urlSource = (UrlBasedCorsConfigurationSource) source;
    CorsConfiguration config = urlSource.getCorsConfigurations().get("/**");

    assertNotNull(config);

    boolean tieneOrigenes =
        (config.getAllowedOriginPatterns() != null && !config.getAllowedOriginPatterns().isEmpty())
        || (config.getAllowedOrigins() != null && !config.getAllowedOrigins().isEmpty());

    assertTrue(tieneOrigenes);

    assertNotNull(config.getAllowedMethods());
    assertTrue(config.getAllowedMethods().contains("GET"));
    assertTrue(config.getAllowedMethods().contains("POST"));
    assertTrue(config.getAllowedMethods().contains("PUT"));
    assertTrue(config.getAllowedMethods().contains("PATCH"));
    assertTrue(config.getAllowedMethods().contains("DELETE"));
    assertTrue(config.getAllowedMethods().contains("OPTIONS"));

    assertNotNull(config.getAllowedHeaders());

    boolean permiteAuthorization =
        config.getAllowedHeaders().contains("Authorization")
        || config.getAllowedHeaders().contains("*");
    boolean permiteContentType =
        config.getAllowedHeaders().contains("Content-Type")
        || config.getAllowedHeaders().contains("*");

    assertTrue(permiteAuthorization);
    assertTrue(permiteContentType);
  }
}