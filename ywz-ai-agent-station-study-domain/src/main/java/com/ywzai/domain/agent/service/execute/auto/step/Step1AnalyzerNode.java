package com.ywzai.domain.agent.service.execute.auto.step;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.model.valobj.enums.AiClientTypeEnumVO;
import com.ywzai.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description: 步骤一:分析任务节点
 * @Version: 1.0
 */
@Service
@Slf4j
public class Step1AnalyzerNode extends AbstractExecuteSupport {
    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {

        AiAgentClientFlowConfigVO taskAiAgentClientFlowConfig = dynamicContext.getClientFlowConfigMap().get(AiClientTypeEnumVO.TASK_ANALYZER_CLIENT.getCode());


        ChatClient taskAnalyzerClient = getChatClientById(taskAiAgentClientFlowConfig.getClientId());

        // 第一阶段：任务分析
        log.info("\n📊 阶段1: 任务状态分析");
        String analysisPrompt = String.format("""
                        **原始用户需求:** %s
                        
                        **当前执行步骤:** 第 %d 步 (最大 %d 步)
                        
                        **历史执行记录:**
                        %s
                        
                        **当前任务:** %s
                        
                        **分析要求:**
                        请深入分析用户的具体需求，制定明确的执行策略：
                        1. 理解用户真正想要什么（如：具体的学习计划、项目列表、技术方案等）
                        2. 分析需要哪些具体的执行步骤（如：搜索信息、检索项目、生成内容等）
                        3. 制定能够产生实际结果的执行策略
                        4. 确保策略能够直接回答用户的问题
                        
                        **输出格式要求:**
                        任务状态分析: [当前任务完成情况的详细分析]
                        执行历史评估: [对已完成工作的质量和效果评估]
                        下一步策略: [具体的执行计划，包括需要调用的工具和生成的内容]
                        完成度评估: [0-100]%%
                        任务状态: [CONTINUE/COMPLETED]
                        """,
                dynamicContext.getCurrentTask(),
                dynamicContext.getStep(),
                dynamicContext.getMaxStep(),
                !dynamicContext.getExecutionHistory().isEmpty() ? dynamicContext.getExecutionHistory().toString() : "[首次执行]",
                dynamicContext.getCurrentTask()
        );

        String analysisResult = taskAnalyzerClient
                .prompt(analysisPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeCommandEntity.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,1024 ))
                .call().content();

        parseAnalysisResult(dynamicContext, analysisResult,executeCommandEntity.getSessionId());
        dynamicContext.setValue("analysisResult", analysisResult);

        // 检查是否已完成
        if (analysisResult.contains("任务状态:") && analysisResult.contains("COMPLETED")||
                analysisResult.contains("完成度评估:") && analysisResult.contains("100%")) {
            dynamicContext.setCompleted(true);
            log.info("✅ 任务分析显示已完成！");
            return router(executeCommandEntity, dynamicContext);
        }

        return router(executeCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            return getBean("step4LogExecutionSummaryNode");
        }
        return getBean("step2PrecisionExecutorNode");
    }


    private void parseAnalysisResult(DefaultExecuteStrategyFactory.DynamicContext dynamicContext, String analysisResult, String sessionId) {
        int step = dynamicContext.getStep();
        log.info("\n📊 === 第 {} 步分析结果 ===", step);

        String[] lines = analysisResult.split("\n");
        String currentSection = "";
        StringBuilder sectionContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains("任务状态分析:")) {
                // 发送上一个section的内容
                sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "analysis_status";
                sectionContent = new StringBuilder();
                log.info("\n🎯 任务状态分析:");
                continue;
            } else if (line.contains("执行历史评估:")) {
                // 发送上一个section的内容
                sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "analysis_history";
                sectionContent = new StringBuilder();
                log.info("\n📈 执行历史评估:");
                continue;
            } else if (line.contains("下一步策略:")) {
                // 发送上一个section的内容
                sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "analysis_strategy";
                sectionContent = new StringBuilder();
                log.info("\n🚀 下一步策略:");
                continue;
            } else if (line.contains("完成度评估:")) {
                // 发送上一个section的内容
                sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "analysis_progress";
                sectionContent = new StringBuilder();
                String progress = line.substring(line.indexOf(":") + 1).trim();
                log.info("\n📊 完成度评估: {}", progress);
                sectionContent.append(line).append("\n");
                continue;
            } else if (line.contains("任务状态:")) {
                // 发送上一个section的内容
                sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
                currentSection = "analysis_task_status";
                sectionContent = new StringBuilder();
                String status = line.substring(line.indexOf(":") + 1).trim();
                if (status.equals("COMPLETED")) {
                    log.info("\n✅ 任务状态: 已完成");
                } else {
                    log.info("\n🔄 任务状态: 继续执行");
                }
                sectionContent.append(line).append("\n");
                continue;
            }

            // 收集当前section的内容
            if (!currentSection.isEmpty()) {
                sectionContent.append(line).append("\n");
                switch (currentSection) {
                    case "analysis_status":
                        log.info("   📋 {}", line);
                        break;
                    case "analysis_history":
                        log.info("   📊 {}", line);
                        break;
                    case "analysis_strategy":
                        log.info("   🎯 {}", line);
                        break;
                    default:
                        log.info("   📝 {}", line);
                        break;
                }
            }
        }

        // 发送最后一个section的内容
        sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
    }

    private void sendAnalysisSubResult(DefaultExecuteStrategyFactory.DynamicContext dynamicContext,
                                       String subType, String content, String sessionId) {
        if (!subType.isEmpty() && !content.isEmpty()) {
            AutoAgentExecuteResultEntity result = AutoAgentExecuteResultEntity.createAnalysisSubResult(
                    dynamicContext.getStep(), subType, content, sessionId);
            sendSseResult(dynamicContext, result);
        }
    }
}
