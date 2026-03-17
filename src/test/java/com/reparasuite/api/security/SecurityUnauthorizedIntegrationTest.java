package com.reparasuite.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

class SecurityUnauthorizedIntegrationTest {

  @Test
  void jwtAuthenticationEntryPointDebeResponder401() throws IOException {
    JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint();

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/v1/ordenes-trabajo");

    MockHttpServletResponse response = new MockHttpServletResponse();

    entryPoint.commence(
        request,
        response,
        new InsufficientAuthenticationException("No autenticado")
    );

    assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());
    assertNotNull(response.getContentAsString());
  }

  @Test
  void jwtAccessDeniedHandlerDebeResponder403() throws IOException {
    JwtAccessDeniedHandler handler = new JwtAccessDeniedHandler();

    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setRequestURI("/api/v1/admin/usuarios");

    MockHttpServletResponse response = new MockHttpServletResponse();

    handler.handle(
        request,
        response,
        new AccessDeniedException("Acceso denegado")
    );

    assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());
    assertNotNull(response.getContentAsString());
  }
}