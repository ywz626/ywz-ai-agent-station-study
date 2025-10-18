package com.ywzai.domain.agent.service.armory.business.data.impl;


import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiClientApiVO;
import com.ywzai.domain.agent.model.valobj.AiClientModelVO;
import com.ywzai.domain.agent.service.armory.business.data.ILoadDataStrategy;
import com.ywzai.domain.agent.service.armory.node.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: Model类型加载数据
 * @Version: 1.0
 */
@Service
@Slf4j
public class AiClientModelLoadDataStrategy implements ILoadDataStrategy {

    @Resource
    protected ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private IAgentRepository agentRepository;

    @Override
    public void loadData(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
        List<String> modelIds = armoryCommandEntity.getCommendList();
        CompletableFuture<List<AiClientApiVO>> apiFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_api) {}", modelIds);
            return agentRepository.getAiClientApiVOListByModelIds(modelIds);
        }, threadPoolExecutor);
        CompletableFuture<List<AiClientModelVO>> modelFuture = CompletableFuture.supplyAsync(() -> {
            log.info("查询配置数据(ai_client_model) {}", modelIds);
            return agentRepository.getAiClientModelVOListByModelIds(modelIds);
        }, threadPoolExecutor);
    }
}
