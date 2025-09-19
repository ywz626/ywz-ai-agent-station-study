package com.ywzai.domain.agent.adapter.repository;

import com.ywzai.domain.agent.model.valobj.*;

import java.util.List;

public interface IAgentRepository {
    List<AiClientApiVO> getAiClientApiVOListByModelIds(List<String> modelIds);

    List<AiClientModelVO> getAiClientModelVOListByModelIds(List<String> modelIds);

    List<AiClientApiVO> getAiClientApiVOListByClientIds(List<String> clientIds);

    List<AiClientModelVO> getAiClientModelVOListByClientIds(List<String> clientIds);

    List<AiClientPromptVO> getAiClientPromptVOListByClientIds(List<String> clientIds);

    List<AiClientAdvisorVO> getAiClientAdvisorVOListByClientIds(List<String> clientIds);

    List<AiClientToolMcpVO> getAiClientToolMcpVOListByClientIds(List<String> clientIds);

    List<AiClientVO> getAiClientVOListByClientIds(List<String> clientIds);
}
