package com.ywzai.domain.agent.service.armory;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommendEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentEnumVO;
import com.ywzai.domain.agent.service.armory.business.data.ILoadDataStrategy;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: 策略模式根节点
 * @Version: 1.0
 */
@Service
public class RootNode extends AbstractArmorySupport {
    @Resource
    private AiClientApiNode aiClientApiNode;

    @Resource
    private Map<String, ILoadDataStrategy> loadDataStrategyMap;

    @Override
    protected void multiThread(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        // 获取命令,可能是 model或client
        String commendType = armoryCommendEntity.getCommendType();
        // 通过命令,获取对应的bean数据注册对象
        AiAgentEnumVO aiAgentEnumVO = AiAgentEnumVO.getByCode(commendType);
        String loadDataStrategy = aiAgentEnumVO.getLoadDataStrategy();

        ILoadDataStrategy loadData = loadDataStrategyMap.get(loadDataStrategy);
        loadData.loadData(armoryCommendEntity, dynamicContext);
    }

    @Override
    protected String doApply(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return router(armoryCommendEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientApiNode;
    }
}
