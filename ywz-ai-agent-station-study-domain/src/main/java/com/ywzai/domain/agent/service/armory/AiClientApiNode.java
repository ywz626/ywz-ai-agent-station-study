package com.ywzai.domain.agent.service.armory;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.model.valobj.AiClientApiVO;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: 注册aiclientapi节点
 * @Version: 1.0
 */
@Slf4j
@Service
public class AiClientApiNode extends AbstractArmorySupport {
    @Resource
    private AiClientToolMcpNode aiClientToolMcpNode;
    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        List<AiClientApiVO> apiList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_API.getDataName());
        if (apiList == null || apiList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client api");
            return router(armoryCommandEntity, dynamicContext);
        }
        for (AiClientApiVO api : apiList) {
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .apiKey(api.getApiKey())
                    .baseUrl(api.getBaseUrl())
                    .completionsPath(api.getCompletionsPath())
                    .embeddingsPath(api.getEmbeddingsPath())
                    .build();
            registerBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName(api.getApiId()), OpenAiApi.class, openAiApi);
        }
//        return "调用成功";
        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception{
        return aiClientToolMcpNode;
    }
}
