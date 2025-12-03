package com.ywzai.infrastructure.dao.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: 提示词配置PO @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientSystemPrompt {
  private Long id;
  private String promptId;
  private String promptName;
  private String promptContent;
  private String description;
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
