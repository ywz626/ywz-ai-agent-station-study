package com.ywzai.domain.agent.service.armory.business.data.impl;


import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.entity.ArmoryCommendEntity;
import com.ywzai.domain.agent.model.valobj.*;
import com.ywzai.domain.agent.service.armory.business.data.ILoadDataStrategy;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: Client类型加载数据
 * @Version: 1.0
 */
@Service
@Slf4j
public class AiClientLoadDataStrategy implements ILoadDataStrategy {

    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private IAgentRepository agentRepository;

    @Override
    public void loadData(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        List<String> clientIds = armoryCommendEntity.getCommendList();
        CompletableFuture<List<AiClientApiVO>> apiFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_api) {}", clientIds);
            return agentRepository.getAiClientApiVOListByClientIds(clientIds);
        }, threadPoolExecutor);
        CompletableFuture<List<AiClientModelVO>> modelFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_model) {}", clientIds);
            return agentRepository.getAiClientModelVOListByClientIds(clientIds);
        }, threadPoolExecutor);
        CompletableFuture<List<AiClientPromptVO>> promptFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_prompt) {}", clientIds);
            return agentRepository.getAiClientPromptVOListByClientIds(clientIds);
        }, threadPoolExecutor);
        CompletableFuture<List<AiClientAdvisorVO>> advisorFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_advisor) {}", clientIds);
            return agentRepository.getAiClientAdvisorVOListByClientIds(clientIds);
        }, threadPoolExecutor);
        CompletableFuture<List<AiClientToolMcpVO>> toolMcpFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_tool_mcp) {}", clientIds);
            return agentRepository.getAiClientToolMcpVOListByClientIds(clientIds);
        }, threadPoolExecutor);
        CompletableFuture<List<AiClientVO>> clientFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client) {}", clientIds);
            return agentRepository.getAiClientVOListByClientIds(clientIds);
        }, threadPoolExecutor);
        CompletableFuture.allOf(apiFuture).thenRun(() -> {
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_API.getDataName(), apiFuture.join());
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_MODEL.getDataName(), modelFuture.join());
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName(), promptFuture.join());
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName(), toolMcpFuture.join());
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName(), advisorFuture.join());
            dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT.getDataName(), clientFuture.join());
        }).join();
    }
}
