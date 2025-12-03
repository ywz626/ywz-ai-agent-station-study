package com.ywzai.domain.agent.service.armory.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @Author: ywz @CreateTime: 2025-09-19 @Description: 策略模式工厂 @Version: 1.0
 */
@Service
public class DefaultArmoryStrategyFactory {

  private final RootNode rootNode;

  public DefaultArmoryStrategyFactory(RootNode rootNode) {
    this.rootNode = rootNode;
  }

  public StrategyHandler<ArmoryCommandEntity, DynamicContext, String> get() {
    return rootNode;
  }

  @Data
  @AllArgsConstructor
  @Builder
  @NoArgsConstructor
  public static class DynamicContext {
    private Map<String, Object> dataObjects = new HashMap<>();

    public <T> void setValue(String key, T value) {
      dataObjects.put(key, value);
    }

    public <T> T getValue(String key) {
      return (T) dataObjects.get(key);
    }
  }
}
