package com.ywzai.domain.agent.service.execute.flow.step;

import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.alibaba.fastjson.JSON;
import com.ywzai.domain.agent.model.entity.AutoAgentExecuteResultEntity;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: ywz @CreateTime: 2025-10-15 @Description: flow节点执行抽象类 @Version: 1.0
 */
@Slf4j
public abstract class AbstractExecuteSupport
        extends AbstractMultiThreadStrategyRouter<
        ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> {

    @Resource
    private ApplicationContext applicationContext;

    @Override
    protected void multiThread(
            ExecuteCommandEntity executeCommandEntity,
            DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext)
            throws ExecutionException, InterruptedException, TimeoutException {
    }

    protected ChatClient getClientById(String clientId) {
        return getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(clientId));
    }

    protected <T> T getBean(String beanName) {
        return (T) applicationContext.getBean(beanName);
    }

    /**
     * 通用的SSE结果发送方法
     *
     * @param dynamicContext 动态上下文
     * @param result         要发送的结果实体
     */
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
}
