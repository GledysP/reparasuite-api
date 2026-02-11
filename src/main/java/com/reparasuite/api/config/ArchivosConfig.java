package com.reparasuite.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class ArchivosConfig implements WebMvcConfigurer {

  @Value("${reparasuite.upload-dir}")
  private String uploadDir;

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
      .addResourceHandler("/files/**")
      .addResourceLocations("file:" + uploadDir + "/");
  }
}
