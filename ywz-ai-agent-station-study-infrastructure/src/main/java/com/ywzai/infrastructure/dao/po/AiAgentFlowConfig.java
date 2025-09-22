package com.ywzai.infrastructure.dao.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: 智能体-客户端关联表PO
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiAgentFlowConfig {
    private Long id;
    private String agentId;
    private String clientId;
    private String clientName;
    private String clientType;
    private String stepPrompt;
    private Integer sequence;
    private LocalDateTime createTime;
}
