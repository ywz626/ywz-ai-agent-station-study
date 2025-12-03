package com.ywzai.api.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-10-17 @Description: 画布配置入参 @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentDrawConfigRequestDTO implements Serializable {
  @Serial private static final long serialVersionUID = 1L;
  private String configId;
  private String configData;
  private String agentId;
  private String configName;
  private String createBy;
  private String description;
  private String updateBy;
}
