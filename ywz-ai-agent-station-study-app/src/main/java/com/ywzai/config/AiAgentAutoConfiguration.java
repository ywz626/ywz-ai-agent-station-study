package com.ywzai.config;


import com.alibaba.fastjson.JSON;
import com.ywzai.domain.agent.model.valobj.AiAgentVO;
import com.ywzai.domain.agent.service.IArmoryService;
import com.ywzai.domain.agent.service.armory.node.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

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


    @Resource
    private IArmoryService armoryService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        try {
            log.info("AI Agent 自动装配开始，配置: {}", aiAgentAutoConfigProperties);

            // 检查配置是否有效
            if (!aiAgentAutoConfigProperties.isEnabled()) {
                log.info("AI Agent 自动装配未启用");
                return;
            }

            List<AiAgentVO> aiAgentVOS = armoryService.acceptArmoryAllAvailableAgents();

            log.info("AI Agent 自动装配完成 {}", JSON.toJSONString(aiAgentVOS));
        } catch (Exception e) {
            log.error("AI Agent 自动装配失败", e);
        }
    }
}


