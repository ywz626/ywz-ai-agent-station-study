package com.ywzai.api;

import com.ywzai.api.dto.AiAgentDrawConfigQueryRequestDTO;
import com.ywzai.api.dto.AiAgentDrawConfigRequestDTO;
import com.ywzai.api.dto.AiAgentDrawConfigResponseDTO;
import com.ywzai.api.response.Response;

import java.util.List;

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

    /**
     * 分页查询拖拉拽流程图配置列表
     *
     * @param request 查询条件与分页参数
     * @return 配置列表
     */
    Response<List<AiAgentDrawConfigResponseDTO>> queryDrawConfigList(AiAgentDrawConfigQueryRequestDTO request);

}
