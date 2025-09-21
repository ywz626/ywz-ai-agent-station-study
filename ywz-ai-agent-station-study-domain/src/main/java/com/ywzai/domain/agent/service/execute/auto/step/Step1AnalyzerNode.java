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
 * @Description: 步骤一:分析任务节点
 * @Version: 1.0
 */
@Service
@Slf4j
public class Step1AnalyzerNode extends AbstractExecuteSupport {
    @Override
    protected String doApply(ExecuteCommentEntity executeCommentEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {

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
                        
                        请分析当前任务状态，评估执行进度，并制定下一步策略。
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
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeCommentEntity.getSessionId() + "-analyzer")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY,1024 ))
                .call().content();

        parseAnalysisResult(dynamicContext.getStep(), analysisResult);
        dynamicContext.setValue("analysisResult", analysisResult);

        // 检查是否已完成
        if (analysisResult.contains("任务状态:") && analysisResult.contains("COMPLETED")||
                analysisResult.contains("完成度评估:") && analysisResult.contains("100%")) {
            dynamicContext.setCompleted(true);
            log.info("✅ 任务分析显示已完成！");
            return router(executeCommentEntity, dynamicContext);
        }

        return router(executeCommentEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommentEntity, DefaultExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommentEntity executeCommentEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            return getBean("step4LogExecutionSummaryNode");
        }
        return getBean("step2PrecisionExecutorNode");
    }


    /**
     * 解析任务分析结果
     */
    private void parseAnalysisResult(int step, String analysisResult) {
        log.info("\n📊 === 第 {} 步分析结果 ===", step);

        String[] lines = analysisResult.split("\n");
        String currentSection = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains("任务状态分析:")) {
                currentSection = "status";
                log.info("\n🎯 任务状态分析:");
                continue;
            } else if (line.contains("执行历史评估:")) {
                currentSection = "history";
                log.info("\n📈 执行历史评估:");
                continue;
            } else if (line.contains("下一步策略:")) {
                currentSection = "strategy";
                log.info("\n🚀 下一步策略:");
                continue;
            } else if (line.contains("完成度评估:")) {
                currentSection = "progress";
                String progress = line.substring(line.indexOf(":") + 1).trim();
                log.info("\n📊 完成度评估: {}", progress);
                continue;
            } else if (line.contains("任务状态:")) {
                currentSection = "task_status";
                String status = line.substring(line.indexOf(":") + 1).trim();
                if (status.equals("COMPLETED")) {
                    log.info("\n✅ 任务状态: 已完成");
                } else {
                    log.info("\n🔄 任务状态: 继续执行");
                }
                continue;
            }

            switch (currentSection) {
                case "status":
                    log.info("   📋 {}", line);
                    break;
                case "history":
                    log.info("   📊 {}", line);
                    break;
                case "strategy":
                    log.info("   🎯 {}", line);
                    break;
                default:
                    log.info("   📝 {}", line);
                    break;
            }
        }
    }
}
