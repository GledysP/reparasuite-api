package com.reparasuite.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reparasuite.proxy")
public class ProxyProperties {

  /**
   * Si true, la app confiará en cabeceras Forwarded / X-Forwarded-*.
   * Debe activarse solo cuando el backend esté detrás de un proxy tuyo
   * (por ejemplo Nginx) que limpie y reescriba correctamente estas cabeceras.
   */
  private boolean trustForwardHeaders = false;

  public boolean isTrustForwardHeaders() {
    return trustForwardHeaders;
  }

  public void setTrustForwardHeaders(boolean trustForwardHeaders) {
    this.trustForwardHeaders = trustForwardHeaders;
  }
}