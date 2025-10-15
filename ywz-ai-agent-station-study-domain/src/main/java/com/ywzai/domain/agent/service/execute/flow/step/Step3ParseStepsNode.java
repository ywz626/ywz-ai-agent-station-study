package com.ywzai.domain.agent.service.execute.flow.step;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-15
 * @Description: 第三个节点抽取计划编排节点中计划
 * @Version: 1.0
 */
@Service
@Slf4j
public class Step3ParseStepsNode extends AbstractExecuteSupport {

    @Resource
    private Step4ExecutesStepsNode step4ExecutesStepsNode;

        /**
     * 执行步骤解析处理逻辑
     *
     * @param executeCommandEntity 执行命令实体，包含执行相关的命令信息
     * @param dynamicContext 动态上下文，用于存储和传递执行过程中的动态数据
     * @return 返回路由方法的执行结果
     * @throws Exception 当执行过程中发生异常时抛出
     */
    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        String planningResult = dynamicContext.getValue("planningResult");
        log.info("开始步骤解析阶段");

        // 解析执行步骤并存储到动态上下文中
        Map<String, String> stepsMap = parseExecutionSteps(planningResult);
        dynamicContext.setValue("stepMap", stepsMap);

        // 构建步骤解析结果的展示内容
        StringBuilder parseResult = new StringBuilder();
        parseResult.append("## 步骤解析结果\n\n");
        parseResult.append(String.format("成功解析 %d 个执行步骤：\n\n", stepsMap.size()));
        for (Map.Entry<String, String> entry : stepsMap.entrySet()) {
            parseResult.append(String.format("- **%s**: %s\n",
                    entry.getKey(), entry.getValue().split("\n")[0]));
        }

        // 创建并发送分析进度结果
        AutoAgentExecuteResultEntity autoAgentExecuteResultEntity = AutoAgentExecuteResultEntity.createAnalysisSubResult(dynamicContext.getStep(),
                "analysis_progress",
                parseResult.toString(),
                executeCommandEntity.getSessionId());
        sendSseResult(dynamicContext,autoAgentExecuteResultEntity);

        // 更新步骤计数并进行路由转发
        dynamicContext.setStep(dynamicContext.getStep() + 1);
        return router(executeCommandEntity, dynamicContext);
    }


    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return step4ExecutesStepsNode;
    }

        /**
     * 解析规划结果中的执行步骤，将每一步提取并存入Map中。
     * <p>
     * 该方法支持两种格式的步骤解析：
     * 1. 标准格式：以 "### 第N步：标题" 开头，后接详细内容；
     * 2. 简单格式：以 "[ ] 第N步：标题" 表示的待办事项列表。
     * </p>
     *
     * @param planningResult 规划结果字符串，可能包含多步执行计划
     * @return 包含解析出的执行步骤的Map，键为"第N步"，值为完整的步骤信息（标题+内容）
     */
    public Map<String, String> parseExecutionSteps(String planningResult) {
        HashMap<String, String> stepsMap = new HashMap<>();

        // 检查输入是否为空或仅包含空白字符
        if (planningResult == null || planningResult.trim().isEmpty()) {
            log.warn("规划结果为空,无法解析步骤");
            return stepsMap;
        }
        try {
            // 使用正则表达式匹配标准格式的步骤："### 第N步：标题\n内容"
            Pattern pattern = Pattern.compile("### (第\\d+步：[^\\n]+)([\\s\\S]*?)(?=### 第\\d+步：|$)");
            Matcher matcher = pattern.matcher(planningResult);
            while (matcher.find()) {
                String stepTitle = matcher.group(1);
                String stepContent = matcher.group(2);

                // 提取步骤编号
                Pattern numberPattern = Pattern.compile("第(\\d+)步：");
                Matcher numberMatcher = numberPattern.matcher(stepTitle);
                if (numberMatcher.find()) {
                    String stepNumber = "第" + numberMatcher.group(1) + "步";
                    String fullStepInfo = stepTitle + "\n" + stepContent;
                    log.info("解析步骤: {} -> {}", stepNumber, fullStepInfo);
                    stepsMap.put(stepNumber, fullStepInfo);
                }
            }
            // 如果没有匹配到详细步骤，尝试匹配简单的步骤列表
            if (stepsMap.isEmpty()) {
                Pattern simpleStepPattern = Pattern.compile("\\[ \\] (第\\d+步：[^\\n]+)");
                Matcher simpleMatcher = simpleStepPattern.matcher(planningResult);

                while (simpleMatcher.find()) {
                    String stepTitle = simpleMatcher.group(1).trim();
                    Pattern numberPattern = Pattern.compile("第(\\d+)步：");
                    Matcher numberMatcher = numberPattern.matcher(stepTitle);

                    if (numberMatcher.find()) {
                        String stepNumber = "第" + numberMatcher.group(1) + "步";
                        String stepResult = stepNumber + "\n" + stepTitle;
                        stepsMap.put(stepNumber, stepResult);
                        log.debug("解析简单步骤: {} -> {}", stepNumber, stepTitle);
                    }
                }
            }

            log.info("成功解析 {} 个执行步骤", stepsMap.size());

        } catch (Exception e) {
            log.error("解析规划结果时发生错误", e);
        }
        return stepsMap;
    }

}
