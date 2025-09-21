package com.ywzai.domain.agent.service.execute.auto.step;


import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.ywzai.domain.agent.model.entity.ExecuteCommentEntity;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description: agent执行逻辑抽象节点
 * @Version: 1.0
 */

public abstract class AbstractExecuteSupport extends AbstractMultiThreadStrategyRouter<ExecuteCommentEntity, DefaultExecuteStrategyFactory.DynamicContext, String> {


    @Resource
    private ApplicationContext applicationContext;

    @Override
    protected void multiThread(ExecuteCommentEntity requestParam, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

    }

    public static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";
    public static final String CHAT_MEMORY_RETRIEVE_SIZE_KEY = "chat_memory_response_size";


    protected <T> T getBean(String beanName){
        return (T) applicationContext.getBean(beanName);
    }

    protected ChatClient getChatClientById(String id){
        return (ChatClient) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT.getBeanName(id));
    }
}
