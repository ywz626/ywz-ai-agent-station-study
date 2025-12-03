package com.ywzai.domain.agent.service.execute.flow;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import com.ywzai.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.service.IExecuteStrategy;
import com.ywzai.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @Author: ywz @CreateTime: 2025-10-15 @Description: Flow执行链路的入口 @Version: 1.0
 */
@Slf4j
@Service
public class FlowAgentExecuteStrategy implements IExecuteStrategy {

  @Resource private DefaultFlowAgentExecuteStrategyFactory defaultFlowAgentExecuteStrategyFactory;

  @Override
  public void execute(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter)
      throws Exception {
    // 获取策略处理链的根节点
    StrategyHandler<
            ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String>
        rootNode = defaultFlowAgentExecuteStrategyFactory.get();

    // 记录请求参数信息
    log.info("最大步数为:{}", requestParameter.getMaxStep());
    log.info("agentId:{}", requestParameter.getAiAgentId());
    log.info("message:{}", requestParameter.getMessage());
    log.info("sessionId:{}", requestParameter.getSessionId());

    // 初始化动态上下文环境
    DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext =
        new DefaultFlowAgentExecuteStrategyFactory.DynamicContext();
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
