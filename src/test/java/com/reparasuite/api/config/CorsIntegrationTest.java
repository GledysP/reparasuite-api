package com.reparasuite.api.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

@SpringBootTest
@ActiveProfiles("test")
class CorsIntegrationTest {

  @Autowired
  private CorsConfigurationSource corsConfigurationSource;

  @Test
  void debeConfigurarCorsConOrigenesMetodosYHeaders() {
    MockHttpServletRequest request =
        new MockHttpServletRequest("OPTIONS", "/api/v1/test");

    CorsConfiguration config =
        corsConfigurationSource.getCorsConfiguration(request);

    assertNotNull(
        config,
        "Debe existir una configuración CORS para las rutas de la API"
    );

    assertNotNull(
        config.getAllowedOriginPatterns(),
        "Los patrones de origen permitidos deben estar configurados"
    );

    assertTrue(
        !config.getAllowedOriginPatterns().isEmpty(),
        "Debe existir al menos un origen permitido"
    );

    assertTrue(
        config.getAllowedOriginPatterns().contains("http://localhost:4200"),
        "Debe permitir el origen del Backoffice en desarrollo"
    );

    assertTrue(
        config.getAllowedOriginPatterns().contains("http://localhost:4300"),
        "Debe permitir el origen del Portal en desarrollo"
    );

    assertNotNull(
        config.getAllowedMethods(),
        "Los métodos HTTP permitidos deben estar configurados"
    );

    assertTrue(config.getAllowedMethods().contains("GET"));
    assertTrue(config.getAllowedMethods().contains("POST"));
    assertTrue(config.getAllowedMethods().contains("PUT"));
    assertTrue(config.getAllowedMethods().contains("PATCH"));
    assertTrue(config.getAllowedMethods().contains("DELETE"));
    assertTrue(config.getAllowedMethods().contains("OPTIONS"));

    assertNotNull(
        config.getAllowedHeaders(),
        "Los headers permitidos deben estar configurados"
    );

    assertTrue(
        config.getAllowedHeaders().contains("Authorization")
            || config.getAllowedHeaders().contains("*"),
        "Debe permitirse el header Authorization"
    );

    assertTrue(
        config.getAllowedHeaders().contains("Content-Type")
            || config.getAllowedHeaders().contains("*"),
        "Debe permitirse el header Content-Type"
    );

    assertTrue(
        Boolean.TRUE.equals(config.getAllowCredentials()),
        "CORS debe permitir credenciales"
    );

    assertNotNull(
        config.getMaxAge(),
        "Debe existir un tiempo máximo de caché para preflight"
    );

    assertTrue(
        config.getMaxAge() > 0,
        "El max-age de CORS debe ser mayor que cero"
    );
  }
}