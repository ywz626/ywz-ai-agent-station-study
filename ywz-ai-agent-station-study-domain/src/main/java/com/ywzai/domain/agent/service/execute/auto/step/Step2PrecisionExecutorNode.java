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
 * @Description: 步骤2: 执行节点
 * @Version: 1.0
 */
@Service
@Slf4j
public class Step2PrecisionExecutorNode extends AbstractExecuteSupport{



    @Override
    protected String doApply(ExecuteCommentEntity executeCommentEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {

        AiAgentClientFlowConfigVO aiAgentClientFlowConfigVO = dynamicContext.getClientFlowConfigMap().get(AiClientTypeEnumVO.PRECISION_EXECUTOR_CLIENT.getCode());
        ChatClient precisionExecutorClient = getChatClientById(aiAgentClientFlowConfigVO.getClientId());

        // 第二阶段：精准执行
        log.info("\n⚡ 阶段2: 精准任务执行");
        String analysisResult = dynamicContext.getValue("analysisResult");
        String executionPrompt = String.format("""
                        **分析师策略:** %s
                        
                        **执行指令:** 根据上述分析师的策略，执行具体的任务步骤。
                        
                        **执行要求:**
                        1. 严格按照策略执行
                        2. 使用必要的工具
                        3. 确保执行质量
                        4. 详细记录过程
                        """, analysisResult);

        String executionResult = precisionExecutorClient
                .prompt(executionPrompt)
                .advisors(a -> a
                        .param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeCommentEntity.getSessionId() + "-executor")
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1024))
                .call().content();

        parseExecutionResult(dynamicContext.getStep(), executionResult);
        dynamicContext.setValue("executionResult", executionResult);
        // 更新执行历史
        String stepSummary = String.format("""
                === 第 %d 步执行记录 ===
                【分析阶段】%s
                【执行阶段】%s
                """, dynamicContext.getStep(), analysisResult, executionResult);
        dynamicContext.getExecutionHistory().append(stepSummary);
        return router(executeCommentEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommentEntity, DefaultExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommentEntity executeCommentEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return getBean("step3QualitySupervisorNode");
    }

    /**
     * 解析执行结果
     */
    private void parseExecutionResult(int step, String executionResult) {
        log.info("\n⚡ === 第 {} 步执行结果 ===", step);

        String[] lines = executionResult.split("\n");
        String currentSection = "";

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.contains("执行目标:")) {
                currentSection = "target";
                log.info("\n🎯 执行目标:");
                continue;
            } else if (line.contains("执行过程:")) {
                currentSection = "process";
                log.info("\n🔧 执行过程:");
                continue;
            } else if (line.contains("执行结果:")) {
                currentSection = "result";
                log.info("\n📈 执行结果:");
                continue;
            } else if (line.contains("质量检查:")) {
                currentSection = "quality";
                log.info("\n🔍 质量检查:");
                continue;
            }

            switch (currentSection) {
                case "target":
                    log.info("   🎯 {}", line);
                    break;
                case "process":
                    log.info("   ⚙️ {}", line);
                    break;
                case "result":
                    log.info("   📊 {}", line);
                    break;
                case "quality":
                    log.info("   ✅ {}", line);
                    break;
                default:
                    log.info("   📝 {}", line);
                    break;
            }
        }
    }
}
