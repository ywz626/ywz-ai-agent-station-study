package com.ywzai.infrastructure.dao.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: modelPO
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientModel {
    private Long id;
    private String modelId;
    private String modelName;
    private String modelType;
    private String apiId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
