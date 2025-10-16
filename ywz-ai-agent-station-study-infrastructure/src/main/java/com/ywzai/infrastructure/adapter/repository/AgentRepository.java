package com.ywzai.infrastructure.adapter.repository;


import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.valobj.*;
import com.ywzai.infrastructure.dao.*;
import com.ywzai.infrastructure.dao.po.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: Agent仓储服务实现类
 * @Version: 1.0
 */
@Repository
@Slf4j
public class AgentRepository implements IAgentRepository {
    @Resource
    private IAiClientApiDao aiClientApiDao;
    @Resource
    private IAiClientModelDao aiClientModelDao;
    @Resource
    private IAiClientConfigDao aiClientConfigDao;
    @Resource
    private IAiClientSystemPromptDao aiClientSystemPromptDao;
    @Resource
    private IAiClientAdvisorDao aiClientAdvisorDao;
    @Resource
    private IAiClientToolMcpDao aiClientToolMcpDao;
    @Resource
    private IAiClientDao aiClientDao;
    @Resource
    private ObjectMapper mapper;
    @Resource
    private IAiAgentFlowConfigDao aiAgentFlowConfigDao;
    @Resource
    private IAiAgentDao aiAgentDao;


    /**
     * 根据AI代理ID获取客户端流程配置映射表
     *
     * @param aiAgentId AI代理ID
     * @return 客户端类型到客户端流程配置VO的映射表，如果aiAgentId为空或查询结果为空则返回空映射表
     */
    @Override
    public Map<String, AiAgentClientFlowConfigVO> getAiAgentFlowConfigMapByAgentId(String aiAgentId) {
        if (aiAgentId == null || aiAgentId.trim().isEmpty()) {
            return Map.of();
        }
        List<AiAgentFlowConfig> aiAgentFlowConfigs = aiAgentFlowConfigDao.queryByAgentId(aiAgentId);
        if (aiAgentFlowConfigs == null || aiAgentFlowConfigs.isEmpty()) {
            return Map.of();
        }
        // 使用 Stream API 将 aiAgentFlowConfigs 转换为 Map
        return aiAgentFlowConfigs.stream()
                .collect(Collectors.toMap(
                        AiAgentFlowConfig::getClientType,  // key: clientId
                        config -> new AiAgentClientFlowConfigVO(  // value: 转换成 AiAgentClientFlowConfigVO
                                config.getClientId(),
                                config.getClientName(),
                                config.getClientType(),
                                config.getStepPrompt(),
                                config.getSequence()
                        )
                ));
    }

    @Override
    public AiAgentVO getAiAgentConfigByAgentId(String agentId) {
        AiAgent aiAgent = aiAgentDao.queryByAgentId(agentId);
        return AiAgentVO.builder()
                .agentId(aiAgent.getAgentId())
                .strategy(aiAgent.getStrategy())
                .build();
    }


    /**
     * 根据客户端ID列表获取对应的AiClientVO对象列表。
     *
     * @param clientIds 客户端ID列表，用于查询对应的客户端信息及其相关配置。
     *                  如果该参数为null或空列表，则直接返回空列表。
     * @return 返回与输入客户端ID对应的有效AiClientVO对象列表。
     * 每个AiClientVO包含客户端基本信息以及关联的模型、提示词、工具和顾问ID列表。
     */
    @Override
    public List<AiClientVO> getAiClientVOListByClientIds(List<String> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return List.of();
        }

        List<AiClientVO> result = new ArrayList<>();
        Set<String> processedClientIds = new HashSet<>();

        for (String clientId : clientIds) {
            if (processedClientIds.contains(clientId)) {
                continue;
            }
            processedClientIds.add(clientId);

            // 1. 查询客户端基本信息
            AiClient aiClient = aiClientDao.queryByClientId(clientId);
            if (aiClient == null || aiClient.getStatus() != 1) {
                continue;
            }

            // 2. 查询客户端相关配置
            List<AiClientConfig> configs = aiClientConfigDao.queryBySourceTypeAndId("client", clientId);

            String modelId = null;
            List<String> promptIdList = new ArrayList<>();
            List<String> mcpIdList = new ArrayList<>();
            List<String> advisorIdList = new ArrayList<>();

            for (AiClientConfig config : configs) {
                if (config.getStatus() != 1) {
                    continue;
                }

                switch (config.getTargetType()) {
                    case "model":
                        modelId = config.getTargetId();
                        break;
                    case "prompt":
                        promptIdList.add(config.getTargetId());
                        break;
                    case "tool_mcp":
                        mcpIdList.add(config.getTargetId());
                        break;
                    case "advisor":
                        advisorIdList.add(config.getTargetId());
                        break;
                }
            }

            // 3. 构建AiClientVO对象
            AiClientVO aiClientVO = AiClientVO.builder()
                    .clientId(aiClient.getClientId())
                    .clientName(aiClient.getClientName())
                    .description(aiClient.getDescription())
                    .modelId(modelId)
                    .promptIdList(promptIdList)
                    .mcpIdList(mcpIdList)
                    .advisorIdList(advisorIdList)
                    .build();

            result.add(aiClientVO);
        }

        return result;
    }


    /**
     * 根据客户端ID列表获取工具MCP信息
     *
     * @param clientIds 客户端ID列表
     * @return 工具MCP信息列表
     */
    @Override
    public List<AiClientToolMcpVO> getAiClientToolMcpVOListByClientIds(List<String> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return List.of();
        }

        List<AiClientToolMcpVO> result = new ArrayList<>();
        Set<String> processedMcpIds = new HashSet<>();

        for (String clientId : clientIds) {
            // 1. 通过clientId查询关联的model配置
            String modelId = aiClientConfigDao.queryModelIdByClientId(clientId);
            // 2. 通过modelId查询关联的tool_mcp配置
            List<String> toolMcpIds = aiClientConfigDao.queryToolMcpIdsByModelId(modelId);
            log.info("查询modelId对应的mcpId列表 {}", toolMcpIds);
            for (String toolMcpId : toolMcpIds) {

                // 避免重复处理相同的mcpId
                if (processedMcpIds.contains(toolMcpId)) {
                    continue;
                }
                processedMcpIds.add(toolMcpId);

                // 2. 通过mcpId查询ai_client_tool_mcp表获取MCP工具配置
                AiClientToolMcp toolMcp = aiClientToolMcpDao.queryByMcpId(toolMcpId);
                if (toolMcp != null && toolMcp.getStatus() == 1) {
                    // 3. 转换为VO对象
                    String transportType = toolMcp.getTransportType();
                    String transportConfig = toolMcp.getTransportConfig();
                    AiClientToolMcpVO mcpVO = AiClientToolMcpVO.builder()
                            .mcpId(toolMcp.getMcpId())
                            .mcpName(toolMcp.getMcpName())
                            .transportType(transportType)
                            .transportConfig(transportConfig)
                            .requestTimeout(toolMcp.getRequestTimeout())
                            .build();
                    try {
                        switch (transportType) {
                            case "stdio":
                                // 解析STDIO配置
                                Map<String, AiClientToolMcpVO.TransportConfigStdio.Stdio> stdio = com.alibaba.fastjson.JSON.parseObject(transportConfig,
                                        new TypeReference<>() {
                                        });
                                AiClientToolMcpVO.TransportConfigStdio transportConfigStdio = new AiClientToolMcpVO.TransportConfigStdio(stdio);
                                mcpVO.setTransportConfigStdio(transportConfigStdio);
                                break;
                            case "sse":
                                AiClientToolMcpVO.TransportConfigSse transportConfigSse = mapper.readValue(transportConfig, AiClientToolMcpVO.TransportConfigSse.class);
                                mcpVO.setTransportConfigSse(transportConfigSse);
                                break;
                        }
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    result.add(mcpVO);
                }
            }
        }
        return result;
    }


    /**
     * 根据客户端ID列表获取顾问信息
     *
     * @param clientIds 客户端ID列表
     * @return 顾问信息列表
     */
    @Override
    public List<AiClientAdvisorVO> getAiClientAdvisorVOListByClientIds(List<String> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return List.of();
        }
        List<AiClientAdvisorVO> result = new ArrayList<>();
        Set<String> advisorIdSet = new HashSet<>();
        for (String clientId : clientIds) {
            List<String> advisorIds = aiClientConfigDao.queryAdvisorsByClientId(clientId);
            for (String advisorId : advisorIds) {
                if (advisorIdSet.contains(advisorId)) {
                    continue;
                }
                advisorIdSet.add(advisorId);
                AiClientAdvisor advisor = aiClientAdvisorDao.queryByAdvisorId(advisorId);
                if (advisor != null && advisor.getStatus() == 1) {
                    AiClientAdvisorVO.RagAnswer ragAnswer = null;
                    AiClientAdvisorVO.ChatMemory chatMemory = null;
                    if (advisor.getAdvisorType().equals("ChatMemory")) {
                        chatMemory = JSON.parseObject(advisor.getExtParam(), AiClientAdvisorVO.ChatMemory.class);
                    } else if (advisor.getAdvisorType().equals("RagAnswer")) {
                        ragAnswer = JSON.parseObject(advisor.getExtParam(), AiClientAdvisorVO.RagAnswer.class);
                    }
                    AiClientAdvisorVO advisorVO = AiClientAdvisorVO.builder()
                            .advisorId(advisor.getAdvisorId())
                            .advisorName(advisor.getAdvisorName())
                            .advisorType(advisor.getAdvisorType())
                            .orderNum(advisor.getOrderNum())
                            .chatMemory(chatMemory)
                            .ragAnswer(ragAnswer)
                            .build();
                    result.add(advisorVO);
                }

            }
        }

        return result;
    }


    /**
     * 根据客户端ID列表获取提示词信息
     *
     * @param clientIds 客户端ID列表
     * @return 提示词信息列表
     */
    @Override
    public Map<String, AiClientPromptVO> getAiClientPromptVOListByClientIds(List<String> clientIds) {
        // 先判空
        if (clientIds == null || clientIds.isEmpty()) {
            return Map.of();
        }
        Map<String, AiClientPromptVO> result = new HashMap<>();
        Set<String> promptIdSet = new HashSet<>();
        for (String clientId : clientIds) {
            // 首先从ai_client_config中获取prompt_id,然后再根据prompt_id获取提示词信息
            List<String> promptIds = aiClientConfigDao.queryPromptIdsByClientId(clientId);
            for (String promptId : promptIds) {
                if (promptIdSet.contains(promptId)) {
                    continue;
                }
                promptIdSet.add(promptId);
                AiClientSystemPrompt aiClientSystemPrompt = aiClientSystemPromptDao.queryByPromptId(promptId);
                if (aiClientSystemPrompt != null && aiClientSystemPrompt.getStatus() == 1) {
                    // 构建AiClientPromptVO对象
                    AiClientPromptVO aiClientPromptVO = AiClientPromptVO.builder()
                            .promptId(aiClientSystemPrompt.getPromptId())
                            .promptName(aiClientSystemPrompt.getPromptName())
                            .promptContent(aiClientSystemPrompt.getPromptContent())
                            .description(aiClientSystemPrompt.getDescription())
                            .build();
                    // 避免重复添加相同的模型配置
                    result.put(aiClientPromptVO.getPromptId(), aiClientPromptVO);
                }
            }
        }
        return result;
    }


    /**
     * 根据客户端ID列表获取模型信息
     *
     * @param clientIds 客户端ID列表
     * @return 模型信息列表
     */
    @Override
    public List<AiClientModelVO> getAiClientModelVOListByClientIds(List<String> clientIds) {
        // 先判空
        if (clientIds == null || clientIds.isEmpty()) {
            return List.of();
        }
        List<AiClientModelVO> result = new ArrayList<>();
        for (String clientId : clientIds) {
            // 首先从ai_client_config中获取modelId,然后再根据modelId获取模型信息
            String modelId = aiClientConfigDao.queryModelIdByClientId(clientId);
            AiClientModel model = aiClientModelDao.queryByModelId(modelId);
            if (model != null && model.getStatus() == 1) {
                List<String> toolMcpIdList = aiClientConfigDao.queryToolMcpIdsByModelId(modelId);
                // 构建AiClientModelVO对象
                log.info("AiClientModelNode: 获取模型信息成功: {}", modelId);
                AiClientModelVO aiClientModelVO = AiClientModelVO.builder()
                        .modelId(model.getModelId())
                        .apiId(model.getApiId())
                        .modelName(model.getModelName())
                        .modelType(model.getModelType())
                        .toolMcpIds(toolMcpIdList)
                        .build();
                // 避免重复添加相同的模型配置
                if (result.stream().noneMatch(vo -> vo.getModelId().equals(aiClientModelVO.getModelId()))) {
                    result.add(aiClientModelVO);
                }

            }
        }
        return result;
    }


    /**
     * 根据客户端ID列表获取api信息
     *
     * @param clientIds 客户端ID列表
     * @return api信息列表
     */
    @Override
    public List<AiClientApiVO> getAiClientApiVOListByClientIds(List<String> clientIds) {
        // 先判空
        if (clientIds == null || clientIds.isEmpty()) {
            return List.of();
        }
        List<AiClientApiVO> result = new ArrayList<>();
        for (String clientId : clientIds) {
            // 首先从ai_client_config中获取modelId,然后再根据modelId获取api信息
            String modelId = aiClientConfigDao.queryModelIdByClientId(clientId);
            AiClientModel model = aiClientModelDao.queryByModelId(modelId);
            if (model != null && model.getStatus() == 1) {
                AiClientApi aiClientApi = aiClientApiDao.queryByApiId(model.getApiId());
                if (aiClientApi != null && aiClientApi.getStatus() == 1) {
                    // 构建AiClientApiVO对象
                    AiClientApiVO aiClientApiVO = AiClientApiVO.builder()
                            .apiId(aiClientApi.getApiId())
                            .baseUrl(aiClientApi.getBaseUrl())
                            .apiKey(aiClientApi.getApiKey())
                            .completionsPath(aiClientApi.getCompletionsPath())
                            .embeddingsPath(aiClientApi.getEmbeddingsPath())
                            .build();
                    // 避免重复添加相同的模型信息
                    if (result.stream().noneMatch(vo -> vo.getApiId().equals(aiClientApiVO.getApiId()))) {
                        result.add(aiClientApiVO);
                    }
                }

            }
        }
        return result;
    }

    /**
     * 根据模型ID列表获取模型信息
     *
     * @param modelIds 模型ID列表
     * @return 模型信息列表
     */
    @Override
    public List<AiClientModelVO> getAiClientModelVOListByModelIds(List<String> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            return List.of();
        }
        List<AiClientModelVO> result = new ArrayList<>();
        for (String modelId : modelIds) {
            AiClientModel model = aiClientModelDao.queryByModelId(modelId);
            if (model != null && model.getStatus() == 1) {
                // 构建AiClientModelVO对象
                AiClientModelVO aiClientModelVO = AiClientModelVO.builder()
                        .modelId(model.getModelId())
                        .apiId(model.getApiId())
                        .modelName(model.getModelName())
                        .modelType(model.getModelType())
                        .build();
                // 避免重复添加相同的模型信息
                if (result.stream().noneMatch(vo -> vo.getApiId().equals(aiClientModelVO.getApiId()))) {
                    result.add(aiClientModelVO);
                }
            }
        }
        return result;
    }


    /**
     * 根据模型ID列表获取API信息
     *
     * @param modelIds 模型ID列表
     * @return API信息列表
     */
    @Override
    public List<AiClientApiVO> getAiClientApiVOListByModelIds(List<String> modelIds) {
        if (modelIds == null || modelIds.isEmpty()) {
            return List.of();
        }

        List<AiClientApiVO> result = new ArrayList<>();
        for (String modelId : modelIds) {
            // 首先根据modelId查询对应的model信息
            AiClientModel model = aiClientModelDao.queryByModelId(modelId);
            if (model != null && model.getStatus() == 1) {
                // 从model信息中获取对应的apiId,然后根据apiId查询对应的api信息
                String apiId = model.getApiId();
                AiClientApi aiClientApi = aiClientApiDao.queryByApiId(apiId);
                if (aiClientApi != null && aiClientApi.getStatus() == 1) {
                    // 构建AiClientApiVO对象
                    AiClientApiVO aiClientApiVO = AiClientApiVO.builder()
                            .apiId(aiClientApi.getApiId())
                            .baseUrl(aiClientApi.getBaseUrl())
                            .apiKey(aiClientApi.getApiKey())
                            .completionsPath(aiClientApi.getCompletionsPath())
                            .embeddingsPath(aiClientApi.getEmbeddingsPath())
                            .build();
                    // 避免重复添加相同的API配置
                    if (result.stream().noneMatch(vo -> vo.getApiId().equals(aiClientApiVO.getApiId()))) {
                        result.add(aiClientApiVO);
                    }
                }
            }
        }
        return result;
    }
}
