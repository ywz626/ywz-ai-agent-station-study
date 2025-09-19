package com.ywzai.domain.agent.model.valobj;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: apiVo对象
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientApiVO {

    private String apiId;
    private String baseUrl;
    private String apiKey;
    private String completionsPath;
    private String embeddingsPath;
}
