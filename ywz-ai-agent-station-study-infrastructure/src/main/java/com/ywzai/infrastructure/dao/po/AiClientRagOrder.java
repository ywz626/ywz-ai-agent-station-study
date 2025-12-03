package com.ywzai.infrastructure.dao.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: rag配置PO @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientRagOrder {
  private Long id;
  private String ragId;
  private String ragName;
  private String knowledgeTag;
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
