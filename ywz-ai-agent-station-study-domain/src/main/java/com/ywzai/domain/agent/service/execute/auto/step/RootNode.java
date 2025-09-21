package com.ywzai.domain.agent.service.execute.auto.step;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.entity.ExecuteCommentEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.Map;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description: 执行策略根节点,负责装配数据
 * @Version: 1.0
 */
@Service("executeRootNode")
@Slf4j
public class RootNode extends AbstractExecuteSupport{

    @Resource
    private IAgentRepository agentRepository;

    @Override
    protected String doApply(ExecuteCommentEntity executeCommentEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        String aiAgentId = executeCommentEntity.getAiAgentId();
        Map<String, AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOMap = agentRepository.getAiAgentFlowConfigMapByAgentId(aiAgentId);
        dynamicContext.setMaxStep(executeCommentEntity.getMaxStep());
        dynamicContext.setClientFlowConfigMap(aiAgentClientFlowConfigVOMap);
        // 上下文信息
        dynamicContext.setExecutionHistory(new StringBuilder());
        // 当前任务信息
        dynamicContext.setCurrentTask(executeCommentEntity.getMessage());
        return router(executeCommentEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ExecuteCommentEntity, DefaultExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommentEntity executeCommentEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return getBean("step1AnalyzerNode");
    }
}
