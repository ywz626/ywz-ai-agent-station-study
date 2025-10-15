package com.ywzai.domain.agent.service.execute.flow.step;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.model.valobj.enums.AiClientTypeEnumVO;
import com.ywzai.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-15
 * @Description: 第四个节点执行计划中的任务
 * @Version: 1.0
 */
@Slf4j
@Service
public class Step4ExecutesStepsNode extends AbstractExecuteSupport {


        /**
     * 执行命令应用策略的核心方法
     *
     * @param executeCommandEntity 执行命令实体，包含执行相关的基础信息
     * @param dynamicContext 动态上下文，用于在执行过程中传递和存储状态信息
     * @return 执行结果描述字符串，表示执行成功或失败的原因
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 获取执行器客户端配置信息
        AiAgentClientFlowConfigVO aiAgentClientFlowConfigVO = dynamicContext.getClientFlowConfigMap().get(AiClientTypeEnumVO.EXECUTOR_CLIENT.getCode());
        String executeClientId = aiAgentClientFlowConfigVO.getClientId();
        dynamicContext.setValue("sessionId", executeCommandEntity.getSessionId());
        log.info("执行计划节点开始");
        ChatClient executeClient = getClientById(executeClientId);
        HashMap<String, String> stepMap = dynamicContext.getValue("stepMap");

        // 检查是否存在可执行的步骤
        if (stepMap == null || stepMap.isEmpty()) {
            log.warn("没有可执行的步骤");
            return "没有步骤可以执行";
        }

        // 按顺序执行所有步骤
        executeStepsInOrder(stepMap, executeClient, dynamicContext);

        // 更新执行状态，标记为完成并进入下一步
        dynamicContext.setStep(dynamicContext.getStep() + 1);
        dynamicContext.setCompleted(true);
        return "所有步骤均已完成";
    }


    /**
     * 按顺序执行步骤
     *
     * @param stepMap        步骤映射，键为步骤标识，值为步骤内容
     * @param executeClient  执行客户端
     * @param dynamicContext 动态上下文环境
     */
    private void executeStepsInOrder(HashMap<String, String> stepMap, ChatClient executeClient, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        ArrayList<Integer> arrayList = new ArrayList<>();
        // 提取步骤序号并排序
        for (String step : stepMap.keySet()) {
            try {
                Pattern pattern = Pattern.compile("第(\\d+)步");
                Matcher matcher = pattern.matcher(step);
                if (matcher.find()) {
                    arrayList.add(Integer.parseInt(matcher.group(1)));
                }
            } catch (Exception e) {
                log.error("解析步骤序号失败", e);
            }
            arrayList.sort(Integer::compareTo);
            // 按序号顺序执行每个步骤
            for (Integer stepNumber : arrayList) {
                String stepKey = "第" + stepNumber + "步";
                String stepContent = null;
                // 查找对应步骤的内容
                for (Map.Entry<String, String> entry : stepMap.entrySet()) {
                    if (entry.getKey().startsWith(stepKey)) {
                        stepContent = entry.getValue();
                        break;
                    }
                }
                if (stepContent != null) {
                    executeStep(executeClient, stepNumber, stepKey, stepContent, dynamicContext);
                } else {
                    log.warn("未找到步骤内容: {}", stepKey);
                }

            }
        }
    }


    /**
     * 执行单个步骤的业务逻辑
     *
     * @param executeClient  用于执行步骤的聊天客户端
     * @param stepNumber     步骤编号
     * @param stepKey        步骤键值标识
     * @param stepContent    步骤内容描述
     * @param dynamicContext 动态上下文环境，用于存储和传递执行过程中的数据
     */
    private void executeStep(ChatClient executeClient, Integer stepNumber, String stepKey, String stepContent, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.info("\n--- 开始执行 {} ---", stepKey);
        log.info("步骤内容: {}", stepContent.substring(0, Math.min(200, stepContent.length())) + "...");
        try {

            // 构建步骤执行的提示词并调用客户端执行
            String userPrompt = buildStepExecutionPrompt(stepContent, dynamicContext);
            String executionResult = executeClient.prompt()
                    .user(userPrompt)
                    .call().content();
            assert executionResult != null;
            log.info("步骤 {} 执行结果: {}", stepNumber, executionResult.substring(0, Math.min(150, executionResult.length())) + "...");

            // 保存执行结果到动态上下文
            dynamicContext.setValue("step" + stepNumber + "Result", executionResult);

            // 创建执行结果实体并发送SSE结果
            AutoAgentExecuteResultEntity stepResult = AutoAgentExecuteResultEntity.createExecutionResult(
                    stepNumber,
                    stepKey + " 执行完成: " + executionResult.substring(0, Math.min(500, executionResult.length())),
                    dynamicContext.getValue("sessionId"));
            sendSseResult(dynamicContext, stepResult);
            // 短暂延迟，避免请求过于频繁
            Thread.sleep(1000);
        } catch (Exception e) {
            log.error("步骤 {} 执行失败", stepNumber, e);
            dynamicContext.setValue("step" + stepNumber + "Error", e.getMessage());
            // 记录错误但继续执行下一步
            handleStepExecutionError(stepNumber, stepKey, e, dynamicContext);
        }
    }


    /**
     * 处理步骤执行错误的方法
     *
     * @param stepNumber     步骤编号
     * @param stepKey        步骤键值
     * @param e              异常对象
     * @param dynamicContext 动态上下文对象
     */
    private void handleStepExecutionError(Integer stepNumber, String stepKey, Exception e, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        log.warn("步骤{} 执行失败,执行回复策略", stepNumber);

        // 统计步骤错误次数
        HashMap<String, Integer> errorStatus = dynamicContext.getValue("stepErrorStats");
        if (errorStatus == null) {
            errorStatus = new HashMap<>();
            dynamicContext.setValue("stepErrorStats", errorStatus);
        }
        errorStatus.put("step" + stepNumber, errorStatus.getOrDefault("step" + stepNumber, 0) + 1);

        // 如果是网络错误，可以尝试重试
        if (e.getMessage() != null && (e.getMessage().contains("timeout") || e.getMessage().contains("connection"))) {
            log.info("检测到网络错误，将在后续重试机制中处理");
        }

        // 设置步骤状态为执行失败
        dynamicContext.setValue("step" + stepNumber + "Status", "FAILED_WITH_ERROR");

        // 创建并发送执行结果
        try {
            AutoAgentExecuteResultEntity executionResult = AutoAgentExecuteResultEntity.createExecutionResult(
                    stepNumber,
                    stepKey + " 执行失败: " + e.getMessage(),
                    dynamicContext.getValue("sessionId")
            );
            sendSseResult(dynamicContext, executionResult);
        } catch (Exception sseEx) {
            log.error("发送步骤执行失败结果失败：{}", sseEx.getMessage(), sseEx);
        }
    }


    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return null;
    }


    private String buildStepExecutionPrompt(String stepContent, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) {
        return "你是一个智能执行助手，需要执行以下步骤:\n\n" +
                "**步骤内容:**\n" +
                stepContent + "\n\n" +
                "**用户原始请求:**\n" +
                dynamicContext.getCurrentTask() + "\n\n" +
                "**执行要求:**\n" +
                "1. 仔细分析步骤内容，理解需要执行的具体任务\n" +
                "2. 如果涉及MCP工具调用，请使用相应的工具\n" +
                "3. 提供详细的执行过程和结果\n" +
                "4. 如果遇到问题，请说明具体的错误信息\n" +
                "5. **重要**: 执行完成后，必须在回复末尾明确输出执行结果，格式如下:\n" +
                "   ```\n" +
                "   === 执行结果 ===\n" +
                "   状态: [成功/失败]\n" +
                "   结果描述: [具体的执行结果描述]\n" +
                "   输出数据: [如果有具体的输出数据，请在此列出]\n" +
                "   ```\n\n" +
                "请开始执行这个步骤，并严格按照要求提供详细的执行报告和结果输出。";
    }
}
