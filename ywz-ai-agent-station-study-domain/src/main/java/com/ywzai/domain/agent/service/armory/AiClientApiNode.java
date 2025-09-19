package com.ywzai.domain.agent.service.armory;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommendEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentEnumVO;
import com.ywzai.domain.agent.model.valobj.AiClientApiVO;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
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
    @Override
    protected String doApply(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        List<AiClientApiVO> apiList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_API.getDataName());
        if (apiList == null || apiList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client api");
            return null;
        }
        for (AiClientApiVO api : apiList) {
            OpenAiApi openAiApi = OpenAiApi.builder()
                    .apiKey(api.getApiKey())
                    .baseUrl(api.getBaseUrl())
                    .completionsPath("/v1/chat/completions")
                    .embeddingsPath("/v1/embeddings")
                    .build();
            registerBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName(api.getApiId()),OpenAiApi.class,openAiApi );
        }
        return "调用成功";
//        return router(armoryCommendEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
