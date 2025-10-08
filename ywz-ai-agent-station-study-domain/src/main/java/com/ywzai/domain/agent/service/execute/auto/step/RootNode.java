package com.ywzai.domain.agent.service.execute.auto.step;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    /**
     * 执行命令应用处理方法
     *
     * @param executeCommandEntity 执行命令实体对象，包含AI代理ID、最大步骤数、消息等信息
     * @param dynamicContext 动态上下文对象，用于存储执行过程中的动态数据
     * @return 返回路由处理结果字符串
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        String aiAgentId = executeCommandEntity.getAiAgentId();
        // 获取AI代理流程配置映射表
        Map<String, AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOMap = agentRepository.getAiAgentFlowConfigMapByAgentId(aiAgentId);
        // 设置动态上下文的最大步骤数和客户端流程配置映射
        dynamicContext.setMaxStep(executeCommandEntity.getMaxStep());
        dynamicContext.setClientFlowConfigMap(aiAgentClientFlowConfigVOMap);
        // 初始化执行历史记录
        dynamicContext.setExecutionHistory(new StringBuilder());
        // 设置当前任务信息
        dynamicContext.setCurrentTask(executeCommandEntity.getMessage());
        // 路由到具体的处理逻辑
        return router(executeCommandEntity, dynamicContext);
    }


    /**
     * 根据执行命令实体和动态上下文获取策略处理器
     *
     * @param executeCommandEntity 执行命令实体，包含具体的执行命令信息
     * @param dynamicContext 动态上下文，提供执行过程中的动态环境信息
     * @return 返回名为"step1AnalyzerNode"的策略处理器bean实例
     * @throws Exception 当获取bean过程中发生异常时抛出
     */
    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, DefaultExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        // 获取名为"step1AnalyzerNode"的bean实例作为策略处理器
        return getBean("step1AnalyzerNode");
    }

}
