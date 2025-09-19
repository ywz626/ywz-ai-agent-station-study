package com.ywzai.domain.agent.model.valobj;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: 提示词VO
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientPromptVO {

    private String promptId;
    private String promptName;
    private String promptContent;
    private String description;
}
