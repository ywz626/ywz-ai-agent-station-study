package com.ywzai.infrastructure.dao.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: 智能体工作计划PO @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentTaskSchedule {
  private Long id;
  private String agentId;
  private String taskName;
  private String description;
  private String cronExpression;
  private String taskParam;
  private Integer status;
  private LocalDateTime createTime;
  private LocalDateTime updateTime;
}
