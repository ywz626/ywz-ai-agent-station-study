package com.ywzai.config;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import com.ywzai.types.common.Constants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description: Client自动配置类
 * @Version: 1.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(AiAgentAutoConfigProperties.class)
@ConditionalOnProperty(prefix = "spring.ai.agent.auto-config", name = "enabled", havingValue = "true")


public class AiAgentAutoConfiguration implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private AiAgentAutoConfigProperties aiAgentAutoConfigProperties;
    @Resource
    private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        List<String> clientIds = aiAgentAutoConfigProperties.getClientIds();
        List<String> clientIdList;
        if (clientIds == null || clientIds.isEmpty()) {
            return ;
        }
        if (clientIds.size() == 1 && clientIds.get(0).contains(Constants.SPLIT)) {
            clientIdList = Arrays.stream(clientIds.get(0).split(Constants.SPLIT))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .toList();
        } else {
            clientIdList = clientIds;
        }

        StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String> rootNode = defaultArmoryStrategyFactory.get();
        try {
            String apply = rootNode.apply(ArmoryCommandEntity.builder()
                    .commendList(clientIdList)
                    .commendType("client")
                    .build(), new DefaultArmoryStrategyFactory.DynamicContext());
            log.info("【AI-AGENT】启动成功，已加载客户端：{}", apply);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
