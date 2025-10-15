package com.ywzai.domain.agent.service.execute.flow.step;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.service.execute.flow.step.factory.DefaultFlowAgentExecuteStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-15
 * @Description: 初始装配数据节点
 * @Version: 1.0
 */
@Service
@Slf4j
public class RootNode extends AbstractExecuteSupport {

    @Resource
    private Step1McpToolsAnalysisNode step1McpToolsAnalysisNode;
    @Resource
    private IAgentRepository repository;

    /**
     * 执行命令应用处理流程
     *
     * @param executeCommandEntity 执行命令实体，包含用户请求信息和AI代理ID等
     * @param dynamicContext 动态上下文对象，用于存储流程执行过程中的动态数据
     * @return 返回路由处理结果
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    protected String doApply(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("\n--- 初始化执行环境 ---");
        // 获取用户请求消息和AI代理ID
        String agentId = executeCommandEntity.getAiAgentId();

        // 根据代理ID获取AI代理客户端流程配置映射
        Map<String, AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOMap = repository.getAiAgentFlowConfigMapByAgentId(agentId);
        dynamicContext.setClientFlowConfigMap(aiAgentClientFlowConfigVOMap);
        dynamicContext.setCurrentTask(executeCommandEntity.getMessage());
        dynamicContext.setValue("startTime", System.currentTimeMillis());
        dynamicContext.setValue("status", "INITIALIZING");

        // 调用路由方法进行后续处理
        return router(executeCommandEntity, dynamicContext);
    }


    @Override
    public StrategyHandler<ExecuteCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext, String> get(ExecuteCommandEntity executeCommandEntity, DefaultFlowAgentExecuteStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return step1McpToolsAnalysisNode;
    }
}
