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
 * @Author: ywz @CreateTime: 2025-09-21 @Description: 步骤一:分析任务节点 @Version: 1.0
 */
@Service
@Slf4j
public class Step1AnalyzerNode extends AbstractExecuteSupport {
  /**
   * 执行任务分析应用逻辑
   *
   * @param executeCommandEntity 执行命令实体，包含执行任务所需的基本信息
   * @param dynamicContext 动态上下文，包含客户端流程配置和执行状态信息
   * @return 返回路由处理结果字符串
   * @throws Exception 当执行过程中发生异常时抛出
   */
  @Override
  protected String doApply(
      ExecuteCommandEntity executeCommandEntity,
      DefaultExecuteStrategyFactory.DynamicContext dynamicContext)
      throws Exception {

    // 获取任务分析器的AI客户端配置
    AiAgentClientFlowConfigVO taskAiAgentClientFlowConfig =
        dynamicContext
            .getClientFlowConfigMap()
            .get(AiClientTypeEnumVO.TASK_ANALYZER_CLIENT.getCode());

    // 根据配置获取任务分析聊天客户端
    ChatClient taskAnalyzerClient = getChatClientById(taskAiAgentClientFlowConfig.getClientId());

    // 第一阶段：任务分析
    log.info("\n📊 阶段1: 任务状态分析");

    // 构建任务分析提示词
    String analysisPrompt =
        String.format(
            taskAiAgentClientFlowConfig.getStepPrompt(),
            dynamicContext.getCurrentTask(),
            dynamicContext.getStep(),
            dynamicContext.getMaxStep(),
            !dynamicContext.getExecutionHistory().isEmpty()
                ? dynamicContext.getExecutionHistory().toString()
                : "[首次执行]",
            dynamicContext.getCurrentTask());

    // 调用AI客户端进行任务分析
    String analysisResult =
        taskAnalyzerClient
            .prompt(analysisPrompt)
            .advisors(
                a ->
                    a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, executeCommandEntity.getSessionId())
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 1536))
            .call()
            .content();
    log.info("\n analysisResult: {}", analysisResult);

    // 解析分析结果并更新动态上下文
    parseAnalysisResult(dynamicContext, analysisResult, executeCommandEntity.getSessionId());
    dynamicContext.setValue("analysisResult", analysisResult);

    // 检查任务是否已完成
    if (analysisResult.contains("任务状态:") && analysisResult.contains("COMPLETED")
        || analysisResult.contains("完成度评估:") && analysisResult.contains("100%")) {
      dynamicContext.setCompleted(true);
      log.info("✅ 任务分析显示已完成！");
      return router(executeCommandEntity, dynamicContext);
    }

    return router(executeCommandEntity, dynamicContext);
  }

  @Override
  public StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.DynamicContext, String>
      get(
          ExecuteCommandEntity executeCommandEntity,
          DefaultExecuteStrategyFactory.DynamicContext dynamicContext)
          throws Exception {
    if (dynamicContext.isCompleted() || dynamicContext.getStep() > dynamicContext.getMaxStep()) {
      return getBean("step4LogExecutionSummaryNode");
    }
    return getBean("step2PrecisionExecutorNode");
  }

  /**
   * 解析分析结果字符串，并根据不同的分析部分将其分段处理和记录日志。 同时在每个分析部分结束时调用方法发送子分析结果。
   *
   * @param dynamicContext 当前执行上下文，用于获取当前步骤等信息
   * @param analysisResult 分析结果文本内容，按行分割进行解析
   * @param sessionId 会话ID，用于标识当前分析过程所属的会话
   */
  private void parseAnalysisResult(
      DefaultExecuteStrategyFactory.DynamicContext dynamicContext,
      String analysisResult,
      String sessionId) {
    int step = dynamicContext.getStep();
    log.info("\n📊 === 第 {} 步分析结果 ===", step);

    String[] lines = analysisResult.split("\n");
    String currentSection = "";
    StringBuilder sectionContent = new StringBuilder();

    // 遍历每一行分析结果，识别不同部分并分别处理
    for (String line : lines) {
      line = line.trim();
      if (line.isEmpty()) continue;

      // 判断当前行是否是某个分析部分的标题行，并切换到对应的部分进行处理
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

      // 收集当前section的内容并在日志中输出详细条目
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

    // 循环结束后将最后收集的一个section内容发送出去
    sendAnalysisSubResult(dynamicContext, currentSection, sectionContent.toString(), sessionId);
  }

  /**
   * 发送分析子结果
   *
   * @param dynamicContext 动态上下文对象，包含执行步骤等信息
   * @param subType 子结果类型
   * @param content 子结果内容
   * @param sessionId 会话ID
   */
  private void sendAnalysisSubResult(
      DefaultExecuteStrategyFactory.DynamicContext dynamicContext,
      String subType,
      String content,
      String sessionId) {
    // 只有当子类型和内容都不为空时才发送结果
    if (!subType.isEmpty() && !content.isEmpty()) {
      // 判断是不是第一次循环的空数据
      AutoAgentExecuteResultEntity result =
          AutoAgentExecuteResultEntity.createAnalysisSubResult(
              dynamicContext.getStep(), subType, content, sessionId);
      sendSseResult(dynamicContext, result);
    }
  }
}
