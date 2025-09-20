package com.ywzai.test;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson2.JSON;
import com.ywzai.domain.agent.model.entity.ArmoryCommendEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentEnumVO;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
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
@Slf4j
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
        System.out.println("测试结果: " + openAiApi);
    }

    @Test
    public void testAiClientModelNode() throws Exception {
        StrategyHandler<ArmoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext, String> rootNode = defaultArmoryStrategyFactory.get();
        String apply = rootNode.apply(
                ArmoryCommendEntity.builder()
                        .commendType(AiAgentEnumVO.AI_CLIENT.getCode())
                        .commendList(Arrays.asList("3001"))
                        .build(),
                new DefaultArmoryStrategyFactory.DynamicContext()
        );
        Prompt prompt = Prompt.builder()
                .messages(new UserMessage("告诉我辽宁省丹东市凤城市今日天气"))
                .build();
        System.out.println(apply);
        OpenAiChatModel chatModel = (OpenAiChatModel) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT_MODEL.getBeanName("2001"));

        System.out.println("测试结果: " + chatModel);
        ChatResponse call = chatModel.call(prompt);
        log.info("输出内容:{}",call.getResult().getOutput().getText());
    }
    @Test
    public void testAiClientAdvisorNode() throws Exception {
        StrategyHandler<ArmoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext, String> rootNode = defaultArmoryStrategyFactory.get();
        String apply = rootNode.apply(
                ArmoryCommendEntity.builder()
                        .commendType(AiAgentEnumVO.AI_CLIENT.getCode())
                        .commendList(Arrays.asList("3001"))
                        .build(),
                new DefaultArmoryStrategyFactory.DynamicContext()
        );
        ChatClient chatClient = (ChatClient) applicationContext.getBean(AiAgentEnumVO.AI_CLIENT.getBeanName("3001"));
        String content = chatClient.prompt(Prompt.builder()
                        .messages(new UserMessage("告诉我大连市今天的天气"))
                        .build())
                .call().content();
        System.out.println("测试结果: " + content);
    }

}
