package com.ywzai.test;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommendEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentEnumVO;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: 注册bean测试
 * @Version: 1.0
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class RegisterBeanTest {

    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;
    @Resource
    private ApplicationContext applicationContext;

    @Test
    public void testAiClientApiNode() throws Exception {
        StrategyHandler<ArmoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext, String> rootNode = defaultArmoryStrategyFactory.get();
        String apply = rootNode.apply(
                ArmoryCommendEntity.builder()
                        .commendType(AiAgentEnumVO.AI_CLIENT.getCode())
                        .commendList(Arrays.asList("3001"))
                        .build(),
                new DefaultArmoryStrategyFactory.DynamicContext()
        );
        System.out.println(apply);
        OpenAiApi openAiApi = (OpenAiApi) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT_API.getBeanName("1001"));
        System.out.println("测试结果: " +  openAiApi);
    }

}
