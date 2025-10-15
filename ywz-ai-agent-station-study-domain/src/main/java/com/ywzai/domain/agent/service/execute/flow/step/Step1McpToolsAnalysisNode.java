package com.ywzai.domain.agent.service.execute.flow.step;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.model.valobj.enums.AiClientTypeEnumVO;
import com.ywzai.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-15
 * @Description: 第一个节点执行MCP工具分析
 * @Version: 1.0
 */
@Service
@Slf4j
public class Step1McpToolsAnalysisNode extends AbstractExecuteSupport {
    @Resource
    private Step2PlanningNode step2PlanningNode;

    /**
     * 执行 MCP 工具能力分析阶段的处理逻辑。该方法属于流程中的第一步，用于分析当前可用的 MCP 工具是否能够满足用户请求，
     * 并生成详细的工具能力评估报告，供后续执行阶段参考。
     *
     * <p>此方法不会执行用户的实际请求，仅进行工具能力分析。</p>
     *
     * @param executeCommandEntity 执行命令实体，包含当前任务的上下文信息和执行参数
     * @param dynamicContext       动态上下文对象，用于在流程中传递状态和数据
     * @return 返回下一步流程的路由结果（由 {@code router} 方法决定）
     * @throws Exception 如果在执行过程中发生异常，则抛出
     */
    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("\n--- 步骤1: MCP工具能力分析（仅分析阶段，不执行用户请求） ---");

        // 设置当前状态为“正在分析工具”
        dynamicContext.setValue("status", "ANALYZING_TOOLS");

        // 获取 MCP 客户端配置信息
        AiAgentClientFlowConfigVO aiAgentClientFlowConfigVO = dynamicContext.getClientFlowConfigMap().get(AiClientTypeEnumVO.TOOL_MCP_CLIENT.getCode());
        String clientId = aiAgentClientFlowConfigVO.getClientId();

        // 获取 MCP 工具客户端实例
        ChatClient mcpToolsClient = getClientById(clientId);

        // 构造 MCP 工具能力分析提示词
        String mcpAnalysisPrompt = String.format(
                """
                        # MCP工具能力分析任务
                        
                        ## 重要说明
                        **注意：本阶段仅进行MCP工具能力分析，不执行用户的实际请求。**\s
                        这是一个纯分析阶段，目的是评估可用工具的能力和适用性，为后续的执行规划提供依据。
                        **在本阶段中，你必须严格禁止任何对MCP服务的真实调用、执行、模拟执行或请求发送行为。**
                        **你只能分析、说明、评估，不可触发、调用或推测调用结果。**
                        
                        ## 用户请求
                        %s
                        
                        ## 分析要求
                        请基于上述实际的MCP工具信息，针对用户请求进行详细的工具能力分析（仅分析，不执行）：
                        
                        ### 1. 工具匹配分析
                        - 分析每个可用工具的核心功能和适用场景
                        - 评估哪些工具能够满足用户请求的具体需求
                        - 标注每个工具的匹配度（高/中/低）
                        - **不要调用或测试任何工具接口，只根据工具说明文档或已知信息进行分析**
                        
                        ### 2. 工具使用指南
                        - 提供每个相关工具的具体调用方式
                        - 说明必需的参数和可选参数
                        - 给出参数的示例值和格式要求
                        - **仅以说明形式呈现调用方式，不执行、不请求、不调用**
                        
                        ### 3. 执行策略建议
                        - 推荐最优的工具组合方案
                        - 建议工具的调用顺序和依赖关系
                        - 提供备选方案和降级策略
                        - **所有建议均基于假设性分析，禁止发起任何真实调用**
                        
                        ### 4. 注意事项
                        - 标注工具的使用限制和约束条件
                        - 提醒可能的错误情况和处理方式
                        - 给出性能优化建议
                        - **禁止访问、测试、调用或触发任何MCP服务，只能进行文字层面的分析说明**
                        
                        ### 5. 分析总结
                        - 明确说明这是分析阶段，不要执行用的任何实际操作
                        - 总结工具能力评估结果
                        - 为后续执行阶段提供建议
                        - **再次强调：本阶段仅限分析，不允许执行、模拟执行或调用MCP服务**
                        
                        请确保分析结果准确、详细、可操作，并再次强调这仅是分析阶段。
                        **在整个输出过程中，不得产生任何形式的真实调用请求、JSON指令、MCP交互或API执行行为。**
                        **输出内容应是纯文本分析报告，而非可执行调用。**
                        """,
                dynamicContext.getCurrentTask()
        );


        // 调用 MCP 客户端获取分析结果
        String content = mcpToolsClient.prompt()
                .user(mcpAnalysisPrompt)
                .call().content();

        log.info("\n⚡ 步骤1: MCP工具能力分析结果: {}\n%s", content);

        // 将分析结果存入上下文
        dynamicContext.setValue("mcp_tools_analysis_result", content);

        // 创建并发送分析子结果
        AutoAgentExecuteResultEntity autoAgentExecuteResultEntity = AutoAgentExecuteResultEntity.createAnalysisSubResult(
                dynamicContext.getStep(),
                "analysis_tools",
                content,
                executeCommandEntity.getSessionId()
        );
        sendSseResult(dynamicContext, autoAgentExecuteResultEntity);

        // 更新步骤编号并进入下一步
        dynamicContext.setStep(dynamicContext.getStep() + 1);
        return router(executeCommandEntity, dynamicContext);
    }


    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return step2PlanningNode;
    }
}
