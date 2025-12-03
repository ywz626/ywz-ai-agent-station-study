package com.ywzai.infrastructure.dao.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: modelPO @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientModel {
  private Long id;
  private String modelId;
  private String modelName;
  private String modelType;
  private String apiId;
  private Integer status;

  /** 模型用途 */
  private String modelUsage;

  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
