package com.ywzai.domain.agent.service.execute.auto.step;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ExecuteCommentEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.model.valobj.enums.AiClientTypeEnumVO;
import com.ywzai.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description: 步骤4: 完成任务生成日志节点
 * @Version: 1.0
 */
@Service
@Slf4j
public class Step4LogExecutionSummaryNode extends AbstractExecuteSupport {
    @Override
    protected String doApply(ExecuteCommentEntity executeCommentEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {


        log.info("\n📊 === 执行第 {} 步 ===", dynamicContext.getStep());

        // 第四阶段：执行总结
        log.info("\n📊 阶段4: 执行总结分析");

        // 记录执行总结
        logExecutionSummary(dynamicContext.getMaxStep(), dynamicContext.getExecutionHistory(), dynamicContext.isCompleted());

        // 如果任务未完成，生成最终总结报告
        if (!dynamicContext.isCompleted()) {
            generateFinalReport(executeCommentEntity, dynamicContext);
        }

        log.info("\n🏁 === 动态多轮执行测试结束 ====");
        log.info("结果:{}", dynamicContext.getExecutionHistory());
        return "ai agent execution summary completed!";
    }

    @Override
    public StrategyHandler<ExecuteCommentEntity, DefaultExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommentEntity executeCommentEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return defaultStrategyHandler;
    }

    /**
     * 输出执行总结信息
     */
    private void logExecutionSummary(int maxSteps, StringBuilder executionHistory, boolean isCompleted) {
        log.info("\n📊 === 动态多轮执行总结 ====");

        int actualSteps = Math.min(maxSteps, executionHistory.toString().split("=== 第").length - 1);
        log.info("📈 总执行步数: {} 步", actualSteps);

        if (isCompleted) {
            log.info("✅ 任务完成状态: 已完成");
        } else {
            log.info("⏸️ 任务完成状态: 未完成（达到最大步数限制）");
        }

        // 计算执行效率
        double efficiency = isCompleted ? 100.0 : (double) actualSteps / maxSteps * 100;
        log.info("📊 执行效率: {:.1f}%", efficiency);
    }

    /**
     * 生成最终总结报告
     */
    private void generateFinalReport(ExecuteCommentEntity requestParameter, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) {
        try {
            log.info("\n--- 生成未完成任务的总结报告 ---");

            String summaryPrompt = String.format("""
                            请对以下未完成的任务执行过程进行总结分析：
                            
                            **原始用户需求:** %s
                            
                            **执行历史:**
                            %s
                            
                            **分析要求:**
                            1. 总结已完成的工作内容
                            2. 分析未完成的原因
                            3. 提出完成剩余任务的建议
                            4. 评估整体执行效果
                            """,
                    requestParameter.getMessage(),
                    dynamicContext.getExecutionHistory().toString());

            // 获取对话客户端 - 使用任务分析客户端进行总结
            AiAgentClientFlowConfigVO aiAgentClientFlowConfigVO = dynamicContext.getClientFlowConfigMap().get(AiClientTypeEnumVO.TASK_ANALYZER_CLIENT.getCode());
            ChatClient chatClient = getChatClientById(aiAgentClientFlowConfigVO.getClientId());

            String summaryResult = chatClient
                    .prompt(summaryPrompt)
                    .advisors(a -> a
                            .param(CHAT_MEMORY_CONVERSATION_ID_KEY, requestParameter.getSessionId() + "-summary")
                            .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
                    .call().content();

            logFinalReport(summaryResult);

            // 将总结结果保存到动态上下文中
            dynamicContext.setValue("finalSummary", summaryResult);

        } catch (Exception e) {
            log.error("生成最终总结报告时出现异常: {}", e.getMessage(), e);
        }
    }

    /**
     * 输出最终总结报告
     */
    private void logFinalReport(String summaryResult) {
        log.info("\n📋 === 最终总结报告 ===");

        String[] lines = summaryResult.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            // 根据内容类型添加不同图标
            if (line.contains("已完成") || line.contains("完成的工作")) {
                log.info("✅ {}", line);
            } else if (line.contains("未完成") || line.contains("原因")) {
                log.info("❌ {}", line);
            } else if (line.contains("建议") || line.contains("推荐")) {
                log.info("💡 {}", line);
            } else if (line.contains("评估") || line.contains("效果")) {
                log.info("📊 {}", line);
            } else {
                log.info("📝 {}", line);
            }
        }
    }
}

