package com.ywzai.domain.agent.service.armory.node;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.model.valobj.enums.AiClientAdvisorTypeEnumVO;
import com.ywzai.domain.agent.model.valobj.AiClientAdvisorVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-20
 * @Description: 顾问角色装配节点
 * @Version: 1.0
 */
@Service
@Slf4j
public class AiClientAdvisorNode extends AbstractArmorySupport{

    @Resource
    private AiClientNode aiClientNode;
    @Resource
    private VectorStore vectorStore;

    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        List<AiClientAdvisorVO> advisorVOList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName());
        if(advisorVOList == null || advisorVOList.isEmpty()){
            log.warn("没有需要被初始化的顾问角色");
            return router(armoryCommandEntity, dynamicContext);
        }
        for (AiClientAdvisorVO advisor : advisorVOList){
            Advisor advisor1 = createAdvisor(advisor, vectorStore);
            registerBean(AiAgentEnumVO.AI_CLIENT_ADVISOR.getBeanName(advisor.getAdvisorId()), Advisor.class, advisor1);
        }
        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientNode;
    }

    private Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore){
        String advisorType = aiClientAdvisorVO.getAdvisorType();
        AiClientAdvisorTypeEnumVO advisorTypeEnum = AiClientAdvisorTypeEnumVO.getByCode(advisorType);
        return advisorTypeEnum.createAdvisor(aiClientAdvisorVO, vectorStore);
    }
}
