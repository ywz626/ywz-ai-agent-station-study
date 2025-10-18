package com.ywzai.api;

import com.ywzai.api.dto.AiAgentDrawConfigRequestDTO;
import com.ywzai.api.dto.AiAgentDrawConfigResponseDTO;
import com.ywzai.api.response.Response;

public interface IAiAgentDrawAdminService {
    Response<String> saveDrawConfig(AiAgentDrawConfigRequestDTO aiAgentDrawConfigRequestDTO);

    /**
     * 获取拖拉拽流程图配置
     *
     * @param configId 配置ID
     * @return 配置数据
     */
    Response<AiAgentDrawConfigResponseDTO> getDrawConfig(String configId);

    /**
     * 删除拖拉拽流程图配置
     *
     * @param configId 配置ID
     * @return 删除结果
     */
    Response<String> deleteDrawConfig(String configId);
}
