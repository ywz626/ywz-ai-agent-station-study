package com.ywzai.domain.agent.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-10-16 @Description: AIAgent对应VO @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentVO {
  private String agentId;

  private String strategy;

  /** 智能体名称 */
  private String agentName;

  /** 描述 */
  private String description;

  /** 渠道类型(agent，chat_stream) */
  private String channel;

  /** 状态(0:禁用,1:启用) */
  private Integer status;
}
