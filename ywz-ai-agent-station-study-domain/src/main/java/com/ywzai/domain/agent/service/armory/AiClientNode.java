package com.ywzai.domain.agent.service.armory;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommendEntity;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.model.valobj.AiClientPromptVO;
import com.ywzai.domain.agent.model.valobj.AiClientVO;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import io.modelcontextprotocol.client.McpSyncClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-20
 * @Description: 客户端装配节点
 * @Version: 1.0
 */
@Service
@Slf4j
public class AiClientNode extends AbstractArmorySupport{
    @Override
    protected String doApply(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        List<AiClientVO> aiClientVOList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT.getDataName());
        if (aiClientVOList == null || aiClientVOList.isEmpty()){
            log.warn("没有需要被初始化的 ai client");
            return null;
        }
        Map<String,AiClientPromptVO> promptVOList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName());
        for (AiClientVO aiClientVO : aiClientVOList){
            // 获取model
            String modelId = aiClientVO.getModelId();
            OpenAiChatModel chatModel = getBean(AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName(modelId));

            // 获取系统提示词
            List<String> promptIdList = aiClientVO.getPromptIdList();
            StringBuilder defaultSystem = new StringBuilder("Ai 智能体 \r\n");
            for (String promptId : promptIdList){
                AiClientPromptVO aiClientPromptVO = promptVOList.get(promptId);
                String promptContent = aiClientPromptVO.getPromptContent();
                defaultSystem.append(promptContent);
            }
            // 获取顾问角色
            List<String> advisorIdList = aiClientVO.getAdvisorIdList();
            List<Advisor> advisorList = new ArrayList<>();
            for (String advisorId : advisorIdList){
                Advisor advisor = getBean(AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName(advisorId));
                advisorList.add(advisor);
            }
            // 获取mcp
            List<String> mcpIdList = aiClientVO.getMcpIdList();
            List<McpSyncClient> mcpSyncClientList = new ArrayList<>();
            for (String mcpId : mcpIdList){
                McpSyncClient mcpSyncClient = getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(mcpId));
                mcpSyncClientList.add(mcpSyncClient);
            }

            // 整合为client
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultSystem(defaultSystem.toString())
                    .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClientList).getToolCallbacks())
                    .defaultAdvisors(advisorList)
                    .build();
            registerBean(AiAgentEnumVO.AI_CLIENT.getBeanName(aiClientVO.getClientId()), ChatClient.class, chatClient);
        }
        return router(armoryCommendEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
