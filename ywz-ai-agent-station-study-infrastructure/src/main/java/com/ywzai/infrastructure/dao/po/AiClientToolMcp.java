package com.ywzai.infrastructure.dao.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: Mcp配置PO
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientToolMcp {
    private Long id;
    private String mcpId;
    private String mcpName;
    private String transportType;
    private String transportConfig;
    private Integer requestTimeout;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
