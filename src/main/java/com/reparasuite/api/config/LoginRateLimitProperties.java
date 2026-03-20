package com.reparasuite.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reparasuite.rate-limit")
public class LoginRateLimitProperties {

  private Scope backoffice = new Scope();
  private Scope portal = new Scope();

  public Scope getBackoffice() {
    return backoffice;
  }

  public void setBackoffice(Scope backoffice) {
    this.backoffice = backoffice;
  }

  public Scope getPortal() {
    return portal;
  }

  public void setPortal(Scope portal) {
    this.portal = portal;
  }

  public static class Scope {
    private int maxFailures = 5;
    private long blockDurationMinutes = 15;
    private long staleEntryHours = 6;

    public int getMaxFailures() {
      return maxFailures;
    }

    public void setMaxFailures(int maxFailures) {
      this.maxFailures = maxFailures;
    }

    public long getBlockDurationMinutes() {
      return blockDurationMinutes;
    }

    public void setBlockDurationMinutes(long blockDurationMinutes) {
      this.blockDurationMinutes = blockDurationMinutes;
    }

    public long getStaleEntryHours() {
      return staleEntryHours;
    }

    public void setStaleEntryHours(long staleEntryHours) {
      this.staleEntryHours = staleEntryHours;
    }
  }
}