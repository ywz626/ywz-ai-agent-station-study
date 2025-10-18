package com.ywzai.domain.agent.service.armory.node;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import com.ywzai.domain.agent.service.armory.business.data.ILoadDataStrategy;
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
@Service("armoryRootNode")
public class RootNode extends AbstractArmorySupport {
    @Resource
    private AiClientApiNode aiClientApiNode;

    @Resource
    private Map<String, ILoadDataStrategy> loadDataStrategyMap;

    @Override
    protected void multiThread(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        String loadDataStrategy = armoryCommandEntity.getLoadDataStrategy();
        ILoadDataStrategy loadData = loadDataStrategyMap.get(loadDataStrategy);
        loadData.loadData(armoryCommandEntity, dynamicContext);
    }

    @Override
    protected String doApply(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return router(armoryCommandEntity, dynamicContext);
    }

    @Override
    public StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommandEntity armoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientApiNode;
    }
}
