package com.ywzai.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-10-16 @Description: 定时任务VO @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentTaskScheduleVO {
  /** 主键ID */
  private Long id;

  /** 智能体ID */
  private String agentId;

  /** 任务描述 */
  private String description;

  /** 时间表达式(如: 0/3 * * * * *) */
  private String cronExpression;

  /** 任务入参配置(JSON格式) */
  private String taskParam;
}
