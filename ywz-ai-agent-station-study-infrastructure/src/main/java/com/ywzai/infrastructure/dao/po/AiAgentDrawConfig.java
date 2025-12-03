package com.ywzai.infrastructure.dao.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-10-17 @Description: 画布对应库表实体类 @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentDrawConfig {
  private Long id;
  private String configId;
  private String configName;
  private String description;
  private String agentId;
  private String configData;
  private int version;
  private int status;
  private String createBy;
  private String updateBy;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
