package com.reparasuite.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.reparasuite.api.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class ReparasuiteApiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ReparasuiteApiApplication.class, args);
  }
}