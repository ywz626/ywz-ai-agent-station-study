package com.ywzai.domain.agent.service.execute.flow.step.factory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.service.execute.flow.step.RootNode;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author: ywz @CreateTime: 2025-10-15 @Description: Flow链路执行工厂 @Version: 1.0
 */
@Service
public class DefaultFlowAgentExecuteStrategyFactory {

  private final RootNode rootNode;

  public DefaultFlowAgentExecuteStrategyFactory(RootNode rootNode) {
    this.rootNode = rootNode;
  }

  public StrategyHandler<
          ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String>
      get() {
    return rootNode;
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  @Builder
  public static class DynamicContext {
    private int step = 1;
    private boolean isCompleted = false;
    private String currentTask;
    private Map<String, Object> executionContext = new HashMap<>();
    private int maxRetries;
    private Map<String, AiAgentClientFlowConfigVO> clientFlowConfigMap;

    public <T> void setValue(String key, T value) {
      executionContext.put(key, value);
    }

    public <T> T getValue(String key) {
      return (T) executionContext.get(key);
    }
  }
}
