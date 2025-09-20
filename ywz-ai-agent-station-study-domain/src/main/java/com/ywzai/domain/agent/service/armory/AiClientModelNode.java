package com.ywzai.domain.agent.service.armory;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommendEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentEnumVO;
import com.ywzai.domain.agent.model.valobj.AiClientModelVO;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import io.modelcontextprotocol.client.McpSyncClient;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.mcp.SyncMcpToolCallback;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-20
 * @Description: Aimodel装配节点
 * @Version: 1.0
 */
@Service
@Slf4j
public class AiClientModelNode extends AbstractArmorySupport {

    @Resource
    private AiClientAdvisorNode aiClientAdvisorNode;


    @Override
    protected String doApply(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        List<AiClientModelVO> aiClientModelVOList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_MODEL.getDataName());
        if (aiClientModelVOList == null || aiClientModelVOList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client model");
            return null;
        }
        for (AiClientModelVO aiClientModelVO : aiClientModelVOList) {
            // 装配mcp工具
            List<String> toolMcpIds = aiClientModelVO.getToolMcpIds();
            List<McpSyncClient> mcpSyncClientList = new ArrayList<>();
            if (toolMcpIds != null && !toolMcpIds.isEmpty()) {
                for (String toolMcpId : toolMcpIds) {
                    McpSyncClient mcpSyncClient = getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(toolMcpId));
                    mcpSyncClientList.add(mcpSyncClient);
                }
            }
            // 获取openaiApi
            OpenAiApi openAiApi = getBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName(aiClientModelVO.getApiId()));
//            OpenAiApi openAiApi = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_API.getBeanName(aiClientModelVO.getApiId()));
            // 构建openaiChatModel
            OpenAiChatModel chatModel = OpenAiChatModel.builder()
                    .openAiApi(openAiApi)
                    .defaultOptions(OpenAiChatOptions.builder()
                            .model(aiClientModelVO.getModelName())
                            .toolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClientList).getToolCallbacks())
                            .build())
                    .build();
            // 注册openaiChatModel
            registerBean(AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName(aiClientModelVO.getModelId()), OpenAiChatModel.class, chatModel);
        }
        return router(armoryCommendEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientAdvisorNode;
    }
}
