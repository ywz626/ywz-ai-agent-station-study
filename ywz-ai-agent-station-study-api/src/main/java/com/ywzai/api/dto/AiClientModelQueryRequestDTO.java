package com.ywzai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientModelQueryRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * API配置ID
     */
    private String apiId;

    /**
     * 模型类型：openai、deepseek、claude
     */
    private String modelType;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

}