package com.ywzai.domain.agent.service.execute.auto.step.factory;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.service.execute.auto.step.RootNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description: 默认执行策略工厂
 * @Version: 1.0
 */
@Service
public class DefaultExecuteStrategyFactory {

    private final RootNode execRootNode;
    public DefaultExecuteStrategyFactory(RootNode execRootNode) {
        this.execRootNode = execRootNode;
    }

    public StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.DynamicContext, String> get() {
        return execRootNode;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class DynamicContext{
        private int step = 1;
        private int maxStep = 1;
        private StringBuilder executionHistory;
        private String currentTask;
        private boolean isCompleted = false;
        private Map<String, AiAgentClientFlowConfigVO> clientFlowConfigMap;
        private Map<String, Object> dataObjects = new HashMap<>();
        public <T> void setValue(String key, T value) {
            dataObjects.put(key, value);
        }

        public <T> T getValue(String key) {
            return (T) dataObjects.get(key);
        }
    }
}
