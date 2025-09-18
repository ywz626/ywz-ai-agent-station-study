package com.ywzai.infrastructure.dao.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: 客户端关联其他模块PO
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientConfig {
    private Long id;
    private String sourceType;
    private String sourceId;
    private String targetType;
    private String targetId;
    private String extParam;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
