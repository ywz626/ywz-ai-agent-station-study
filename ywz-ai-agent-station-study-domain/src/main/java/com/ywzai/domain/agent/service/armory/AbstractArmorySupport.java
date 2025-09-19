package com.ywzai.domain.agent.service.armory;


import cn.bugstack.wrench.design.framework.tree.AbstractMultiThreadStrategyRouter;
import com.ywzai.domain.agent.model.entity.ArmoryCommendEntity;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: 策略树配置抽象类
 * @Version: 1.0
 */
public abstract class AbstractArmorySupport extends AbstractMultiThreadStrategyRouter<ArmoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext,String> {
    @Override
    protected void multiThread(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {

    }
}
