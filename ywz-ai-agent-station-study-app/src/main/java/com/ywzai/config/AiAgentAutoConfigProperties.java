package com.ywzai.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @Author: ywz @CreateTime: 2025-09-21 @Description: Client自动配置实体类 @Version: 1.0
 */
@Data
@ConfigurationProperties(prefix = "spring.ai.agent.auto-config")
public class AiAgentAutoConfigProperties {
  private boolean enabled = false;
  private List<String> clientIds;
}
