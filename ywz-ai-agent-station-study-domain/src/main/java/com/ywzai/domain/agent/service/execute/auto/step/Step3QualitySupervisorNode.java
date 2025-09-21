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
 * @Description: 步骤3: 质量检测节点
 * @Version: 1.0
 */
@Service
@Slf4j
public class Step3QualitySupervisorNode extends AbstractExecuteSupport{

    @Override
    protected String doApply(ExecuteCommentEntity executeCommentEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        AiAgentClientFlowConfigVO qualityClientFlowConfig = dynamicContext.getClientFlowConfigMap().get(AiClientTypeEnumVO.QUALITY_SUPERVISOR_CLIENT.getCode());
        ChatClient qualitySupervisorClient = getChatClientById(qualityClientFlowConfig.getClientId());

        // 第三阶段：质量监督
        log.info("\n🔍 阶段3: 质量监督检查");
        String supervisionPrompt = String.format("""
                        **用户原始需求:** %s
                        
                        **执行结果:** %s
                        
                        **监督要求:** 请评估执行结果的质量，识别问题，并提供改进建议。
                        """, executeCommentEntity.getMessage(), dynamicContext.getExecutionHistory());

        String supervisionResult = qualitySupervisorClient
                .prompt(supervisionPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeCommentEntity.getSessionId() + "-supervisor")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                .call().content();

        parseSupervisionResult(dynamicContext.getStep(), supervisionResult);

        // 根据监督结果决定是否需要重新执行
        if (supervisionResult.contains("FAIL") && supervisionResult.contains("是否通过:")) {
            log.info("❌ 质量检查未通过，需要重新执行");
            dynamicContext.setCurrentTask("根据质量监督的建议重新执行任务");
        } else if (supervisionResult.contains("OPTIMIZE") && supervisionResult.contains("是否通过:")) {
            log.info("🔧 质量检查建议优化，继续改进");
            dynamicContext.setCurrentTask("根据质量监督的建议优化执行结果");
        } else if (supervisionResult.contains("是否通过:") && supervisionResult.contains("PASS")) {
            log.info("✅ 质量检查通过");
            dynamicContext.setCompleted(true);
        } else {
            // 如果没有任何匹配的情况，可以记录一个警告或处理默认情况
            log.warn("⚠️ 无法识别的质量检查结果: {}", supervisionResult);
            dynamicContext.setCurrentTask("无法识别的质量检查结果，请手动处理");
        }
//        log.info("检测结果: {}", supervisionResult);

        String analysisResult = dynamicContext.getValue("analysisResult");
        String executionResult = dynamicContext.getValue("executionResult");
        // 更新执行历史
        String stepSummary = String.format("""
                        === 第 %d 步完整记录 ===
                        【分析阶段】%s
                        【执行阶段】%s
                        【监督阶段】%s
                        """, dynamicContext.getStep(), analysisResult, executionResult, supervisionResult);

        dynamicContext.getExecutionHistory().append(stepSummary);

        // 增加步骤计数
        dynamicContext.setStep(dynamicContext.getStep() + 1);

        // 如果任务已完成或达到最大步数，进入总结阶段
        if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            return router(executeCommentEntity, dynamicContext);
        }
        // 否则继续下一轮执行，返回到Step1AnalyzerNode
        return router(executeCommentEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommentEntity, DefaultExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommentEntity executeCommentEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
            return getBean("step4LogExecutionSummaryNode");
        }

        return getBean("step1AnalyzerNode");
    }

    /**
     * 解析监督结果
     */
    private void parseSupervisionResult(int step, String supervisionResult) {
        log.info("\n🔍 === 第 {} 步监督结果 ===", step);

        String[] lines = supervisionResult.split("\n");
        String currentSection = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains("质量评估:")) {
                currentSection = "assessment";
                log.info("\n📊 质量评估:");
                continue;
            } else if (line.contains("问题识别:")) {
                currentSection = "issues";
                log.info("\n⚠️ 问题识别:");
                continue;
            } else if (line.contains("改进建议:")) {
                currentSection = "suggestions";
                log.info("\n💡 改进建议:");
                continue;
            } else if (line.contains("质量评分:")) {
                currentSection = "score";
                String score = line.substring(line.indexOf(":") + 1).trim();
                log.info("\n📊 质量评分: {}", score);
                continue;
            } else if (line.contains("是否通过:")) {
                currentSection = "pass";
                String status = line.substring(line.indexOf(":") + 1).trim();
                if (status.equals("PASS")) {
                    log.info("\n✅ 检查结果: 通过");
                } else if (status.equals("FAIL")) {
                    log.info("\n❌ 检查结果: 未通过");
                } else {
                    log.info("\n🔧 检查结果: 需要优化");
                }
                continue;
            }

            switch (currentSection) {
                case "assessment":
                    log.info("   📋 {}", line);
                    break;
                case "issues":
                    log.info("   ⚠️ {}", line);
                    break;
                case "suggestions":
                    log.info("   💡 {}", line);
                    break;
                default:
                    log.info("   📝 {}", line);
                    break;
            }
        }
    }


}
