package com.ywzai.config;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GuavaConfig {

  @Bean(name = "cache")
  public Cache<String, String> cache() {
    return CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.SECONDS).build();
  }
}
