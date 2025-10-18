package com.ywzai.domain.agent.service.armory.node;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.model.valobj.AiClientPromptVO;
import com.ywzai.domain.agent.model.valobj.AiClientVO;
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
        /**
     * 执行AI客户端的初始化应用逻辑。该方法会从动态上下文中获取AI客户端配置信息，
     * 并根据配置创建对应的ChatClient实例，注册到Spring容器中。
     *
     * @param armoryCommandEntity 命令实体对象，用于路由后续处理流程
     * @param dynamicContext 动态上下文，包含当前策略执行所需的各类数据和组件引用
     * @return 路由结果字符串，表示下一步要执行的操作或状态
     * @throws Exception 当没有可用的AI客户端时抛出异常
     */
    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 获取所有待初始化的 AI 客户端列表
        List<AiClientVO> aiClientVOList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT.getDataName());
        if (aiClientVOList == null || aiClientVOList.isEmpty()){
            log.warn("没有需要被初始化的 ai client");
            throw new Exception("没有需要被初始化的 ai client");
        }

        // 获取系统提示词映射表
        Map<String,AiClientPromptVO> promptVOList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName());

        // 遍历每个AI客户端配置并进行初始化构建
        for (AiClientVO aiClientVO : aiClientVOList){
            // 获取模型ID，并通过工厂获取对应的聊天模型实例
            String modelId = aiClientVO.getModelId();
            OpenAiChatModel chatModel = getBean(AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName(modelId));

            // 构建默认系统提示语：拼接基础描述与各提示内容
            List<String> promptIdList = aiClientVO.getPromptIdList();
            StringBuilder defaultSystem = new StringBuilder("Ai 智能体 \r\n");
            for (String promptId : promptIdList){
                AiClientPromptVO aiClientPromptVO = promptVOList.get(promptId);
                String promptContent = aiClientPromptVO.getPromptContent();
                defaultSystem.append(promptContent);
            }

            // 获取顾问角色列表，并转换为实际Advisor实例集合
            List<String> advisorIdList = aiClientVO.getAdvisorIdList();
            List<Advisor> advisorList = new ArrayList<>();
            for (String advisorId : advisorIdList){
                Advisor advisor = getBean(AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName(advisorId));
                advisorList.add(advisor);
            }

            // 获取MCP工具客户端列表，并转换为实际McpSyncClient实例集合
            List<String> mcpIdList = aiClientVO.getMcpIdList();
            List<McpSyncClient> mcpSyncClientList = new ArrayList<>();
            for (String mcpId : mcpIdList){
                McpSyncClient mcpSyncClient = getBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(mcpId));
                mcpSyncClientList.add(mcpSyncClient);
            }

            // 使用以上组件整合构造ChatClient实例，并将其注册进Spring容器
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultSystem(defaultSystem.toString())
                    .defaultToolCallbacks(new SyncMcpToolCallbackProvider(mcpSyncClientList).getToolCallbacks())
                    .defaultAdvisors(advisorList)
                    .build();
            registerBean(AiAgentEnumVO.AI_CLIENT.getBeanName(aiClientVO.getClientId()), ChatClient.class, chatClient);
        }

        // 执行命令路由逻辑并返回结果
        return router(armoryCommandEntity, dynamicContext);
    }


    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }
}
