package com.reparasuite.api.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(RefreshTokenCookieProperties.class)
public class RefreshTokenConfig {
}