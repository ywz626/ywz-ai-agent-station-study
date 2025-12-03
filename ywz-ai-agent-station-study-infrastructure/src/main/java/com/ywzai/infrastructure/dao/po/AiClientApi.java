package com.ywzai.infrastructure.dao.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: AiApi相关配置PO @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientApi {
  private Long id;
  private String apiId;
  private String baseUrl;
  private String apiKey;
  private String completionsPath;
  private String embeddingsPath;
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
