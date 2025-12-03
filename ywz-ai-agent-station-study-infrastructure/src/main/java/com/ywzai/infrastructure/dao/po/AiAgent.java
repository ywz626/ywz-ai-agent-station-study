package com.ywzai.infrastructure.dao.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: AiAgent数据库对象 @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgent {

  private Long id;
  private String agentId;
  private String agentName;
  private String description;
  private String channel;
  private String strategy;
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
