package com.ywzai.domain.agent.service.execute.fixed;

import com.alibaba.fastjson.JSON;
import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.service.IExecuteStrategy;
import com.ywzai.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @Author: ywz @CreateTime: 2025-10-16 @Description: 固定流程的Aiagent执行链路 @Version: 1.0
 */
@Service
@Slf4j
public class FixedAgentExecuteStrategy implements IExecuteStrategy {
  @Resource private IAgentRepository repository;
  @Resource private ApplicationContext applicationContext;

  public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
  public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";

  /**
   * 固定执行智能体对话命令
   *
   * @param requestParameter 包含用户请求信息、会话ID和AI代理ID的执行命令实体
   * @param emitter 用于异步响应的响应体发射器
   * @throws Exception 执行过程中可能抛出的异常
   */
  @Override
  public void execute(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter)
      throws Exception {
    String userRequest = requestParameter.getMessage();
    String agentId = requestParameter.getAiAgentId();
    // 根据代理ID获取AI代理流程配置列表
    List<AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOLIst =
        repository.getAiAgentFlowConfigListByAgentId(agentId);
    String message = "";
    // 遍历流程配置，依次调用对应的客户端进行对话处理
    for (AiAgentClientFlowConfigVO aiAgentClientFlowConfigVO : aiAgentClientFlowConfigVOLIst) {
      ChatClient defaultClient = getClientById(aiAgentClientFlowConfigVO.getClientId());
      message =
          defaultClient
              .prompt(userRequest + "," + message)
              .system(s -> s.param("current_date", LocalDate.now().toString()))
              .advisors(
                  a ->
                      a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, requestParameter.getSessionId())
                          .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 50))
              .call()
              .content();
      log.info("智能体对话进行，客户端ID {}", requestParameter.getAiAgentId());
    }
    log.info("智能体对话结束，客户端ID {},结果:{}", requestParameter.getAiAgentId(), message);
    // 发送最终结果通知（确保 content 不为空）
    if (message != null && !message.trim().isEmpty()) {
      sendFinalResult(emitter, message, requestParameter.getSessionId());
    }

    // 发送完成标识
    sendCompleteResult(emitter, requestParameter.getSessionId());
  }

  protected void sendSseResult(
      DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext,
      AutoAgentExecuteResultEntity result) {
    try {
      ResponseBodyEmitter emitter = dynamicContext.getValue("emitter");
      if (emitter != null) {
        // 发送SSE格式的数据
        String sseData = "data: " + JSON.toJSONString(result) + "\n\n";
        emitter.send(sseData);
      }
    } catch (IOException e) {
      log.error("发送SSE结果失败：{}", e.getMessage(), e);
    }
  }

  protected ChatClient getClientById(String clientId) {
    return getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(clientId));
  }

  protected <T> T getBean(String beanName) {
    return (T) applicationContext.getBean(beanName);
  }

  /** 发送最终结果到流式输出 */
  private void sendFinalResult(ResponseBodyEmitter emitter, String content, String sessionId) {
    try {
      AutoAgentExecuteResultEntity result =
          AutoAgentExecuteResultEntity.createSummaryResult(content, sessionId);
      String sseData = "data: " + com.alibaba.fastjson2.JSON.toJSONString(result) + "\n\n";
      emitter.send(sseData);
      log.info("✅ 已发送最终结果");
    } catch (Exception e) {
      log.error("发送最终结果失败：{}", e.getMessage(), e);
    }
  }

  /** 发送完成标识到流式输出 */
  private void sendCompleteResult(ResponseBodyEmitter emitter, String sessionId) {
    try {
      AutoAgentExecuteResultEntity result =
          AutoAgentExecuteResultEntity.createCompleteResult(sessionId);
      String sseData = "data: " + com.alibaba.fastjson2.JSON.toJSONString(result) + "\n\n";
      emitter.send(sseData);
      log.info("✅ 已发送完成标识");
    } catch (Exception e) {
      log.error("发送完成标识失败：{}", e.getMessage(), e);
    }
  }
}
