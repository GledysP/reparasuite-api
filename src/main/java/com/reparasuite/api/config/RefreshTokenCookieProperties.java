package com.reparasuite.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reparasuite.refresh-token")
public class RefreshTokenCookieProperties {

  private long expDays = 14;
  private String cookieName = "rs_refresh_token";
  private boolean cookieSecure = false;
  private String cookieSameSite = "Lax";
  private String cookiePath = "/";

  public long getExpDays() {
    return expDays;
  }

  public void setExpDays(long expDays) {
    this.expDays = expDays;
  }

  public String getCookieName() {
    return cookieName;
  }

  public void setCookieName(String cookieName) {
    this.cookieName = cookieName;
  }

  public boolean isCookieSecure() {
    return cookieSecure;
  }

  public void setCookieSecure(boolean cookieSecure) {
    this.cookieSecure = cookieSecure;
  }

  public String getCookieSameSite() {
    return cookieSameSite;
  }

  public void setCookieSameSite(String cookieSameSite) {
    this.cookieSameSite = cookieSameSite;
  }

  public String getCookiePath() {
    return cookiePath;
  }

  public void setCookiePath(String cookiePath) {
    this.cookiePath = cookiePath;
  }
}