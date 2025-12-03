package com.ywzai.domain.agent.service.execute.auto;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import com.ywzai.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.service.IExecuteStrategy;
import com.ywzai.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @Author: ywz @CreateTime: 2025-09-21 @Description: @Version: 1.0
 */
@Service
@Slf4j
public class AutoAgentExecuteStrategy implements IExecuteStrategy {

  @Resource private DefaultExecuteStrategyFactory defaultExecuteStrategyFactory;

  /**
   * 执行命令处理逻辑
   *
   * @param requestParameter 包含执行命令所需参数的实体对象，包括最大步数、AI代理ID、消息内容和会话ID等信息
   * @param emitter 用于向客户端发送响应数据的ResponseBodyEmitter对象
   * @throws Exception 执行过程中可能抛出的异常
   */
  @Override
  public void execute(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter)
      throws Exception {
    // 获取策略处理链的根节点
    StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.DynamicContext, String>
        rootNode = defaultExecuteStrategyFactory.get();

    // 记录请求参数信息
    log.info("最大步数为:{}", requestParameter.getMaxStep());
    log.info("agentId:{}", requestParameter.getAiAgentId());
    log.info("message:{}", requestParameter.getMessage());
    log.info("sessionId:{}", requestParameter.getSessionId());

    // 初始化动态上下文环境
    DefaultExecuteStrategyFactory.DynamicContext dynamicContext =
        new DefaultExecuteStrategyFactory.DynamicContext();
    dynamicContext.setCurrentTask(requestParameter.getMessage());
    dynamicContext.setCompleted(false);
    dynamicContext.setExecutionHistory(new StringBuilder());
    dynamicContext.setMaxStep(
        requestParameter.getMaxStep() != null ? requestParameter.getMaxStep() : 3);
    dynamicContext.setStep(1);
    dynamicContext.setValue("emitter", emitter);

    // 执行策略链处理
    String apply = rootNode.apply(requestParameter, dynamicContext);
    log.info("测试结果:{}", apply);

    // 发送完成标识
    try {
      AutoAgentExecuteResultEntity completeResult =
          AutoAgentExecuteResultEntity.createCompleteResult(requestParameter.getSessionId());
      // 发送SSE格式的数据
      String sseData = "data: " + JSON.toJSONString(completeResult) + "\n\n";
      emitter.send(sseData);
    } catch (Exception e) {
      log.error("发送完成标识失败：{}", e.getMessage(), e);
    }
  }
}
