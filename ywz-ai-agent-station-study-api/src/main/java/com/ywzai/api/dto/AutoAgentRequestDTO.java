package com.ywzai.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description:
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AutoAgentRequestDTO {

    @Serial
    private static final long serialVersionUID = 1L;

    private String aiAgentId;
    private String sessionId;
    private String message;
    private Integer maxStep;
}
