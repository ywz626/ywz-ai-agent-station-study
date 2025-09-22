package com.ywzai.domain.agent.model.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description: 执行命令配置类
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteCommandEntity {

    private String aiAgentId;
    private String message;
    private Integer maxStep;
    private String sessionId;

}
