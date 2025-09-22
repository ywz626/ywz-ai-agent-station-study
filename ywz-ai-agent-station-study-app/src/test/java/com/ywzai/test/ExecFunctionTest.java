package com.ywzai.test;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.ywzai.domain.agent.service.execute.auto.step.factory.DefaultExecuteStrategyFactory;
import jakarta.annotation.Resource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description: 测试agent执行链路
 * @Version: 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ExecFunctionTest {


    @Resource
    private DefaultExecuteStrategyFactory defaultExecuteStrategyFactory;
    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Before
    public void init() throws Exception {
        StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> rootNode = defaultArmoryStrategyFactory.get();
        String apply = rootNode.apply(
                ArmoryCommandEntity.builder()
                        .commendType(AiAgentEnumVO.AI_CLIENT.getCode())
                        .commendList(Arrays.asList("3101", "3102","3103"))
                        .build(),
                new DefaultArmoryStrategyFactory.DynamicContext()
        );
    }

    @Test
    public void test() throws Exception {
        StrategyHandler<ExecuteCommandEntity, DefaultExecuteStrategyFactory.DynamicContext, String> rootNode = defaultExecuteStrategyFactory.get();
        String apply = rootNode.apply(
                ExecuteCommandEntity.builder()
                        .aiAgentId("3")
                        .sessionId("session-id-" + System.currentTimeMillis())
                        .maxStep(5)
                        .message("搜索小傅哥，技术项目列表。编写成一份文档，说明不同项目的学习目标，以及不同阶段的伙伴应该学习哪个项目。")
                        .build(),
                new DefaultExecuteStrategyFactory.DynamicContext()
        );
        System.out.println("测试结果: " + apply);
    }
}
