package com.ywzai.domain.agent.service.armory.business.data;


import com.ywzai.domain.agent.model.entity.ArmoryCommendEntity;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: 数据加载接口
 * @Version: 1.0
 */
public interface ILoadDataStrategy {

    void loadData(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext);
}
