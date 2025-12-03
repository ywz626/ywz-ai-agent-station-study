package com.ywzai.infrastructure.dao.po;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: 智能体-客户端关联表PO @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentFlowConfig {
  /** 配置ID，唯一标识一个流程配置 */
  private Long id;

  /** 代理ID，关联到具体的AI代理 */
  private String agentId;

  /** 客户端ID，标识使用该配置的客户端 */
  private String clientId;

  /** 客户端名称，客户端的可读名称 */
  private String clientName;

  /** 客户端类型，区分不同类型的客户端 */
  private String clientType;

  /** 步骤提示词，定义该流程步骤的提示信息 */
  private String stepPrompt;

  /** 执行序列，定义流程步骤的执行顺序 */
  private Integer sequence;

  private int status;

  /** 创建时间，记录配置的创建时间 */
  private LocalDateTime createTime;
}
