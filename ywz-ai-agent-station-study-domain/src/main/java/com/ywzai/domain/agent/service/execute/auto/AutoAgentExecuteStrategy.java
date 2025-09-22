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
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description:
 * @Version: 1.0
 */
@Service
@Slf4j
public class AutoAgentExecuteStrategy implements IExecuteStrategy {

    @Resource
    private DefaultExecuteStrategyFactory defaultExecuteStrategyFactory;

    @Override
    public void execute(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter) throws Exception {
        StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.DynamicContext, String> rootNode = defaultExecuteStrategyFactory.get();
        log.info("最大步数为:{}", requestParameter.getMaxStep());
        log.info("agentId:{}", requestParameter.getAiAgentId());
        log.info("message:{}", requestParameter.getMessage());
        log.info("sessionId:{}", requestParameter.getSessionId());
        DefaultExecuteStrategyFactory.DynamicContext dynamicContext = new DefaultExecuteStrategyFactory.DynamicContext();
        dynamicContext.setCurrentTask(requestParameter.getMessage());
        dynamicContext.setCompleted(false);
        dynamicContext.setExecutionHistory(new StringBuilder());
        dynamicContext.setMaxStep(requestParameter.getMaxStep() != null ? requestParameter.getMaxStep() : 3);
        dynamicContext.setStep(1);
        dynamicContext.setValue("emitter",emitter);
        String apply = rootNode.apply(requestParameter, dynamicContext);
        log.info("测试结果:{}", apply);

        // 发送完成标识
        try {
            AutoAgentExecuteResultEntity completeResult = AutoAgentExecuteResultEntity.createCompleteResult(requestParameter.getSessionId());
            // 发送SSE格式的数据
            String sseData = "data: " + JSON.toJSONString(completeResult) + "\n\n";
            emitter.send(sseData);
        } catch (Exception e) {
            log.error("发送完成标识失败：{}", e.getMessage(), e);
        }
    }
}