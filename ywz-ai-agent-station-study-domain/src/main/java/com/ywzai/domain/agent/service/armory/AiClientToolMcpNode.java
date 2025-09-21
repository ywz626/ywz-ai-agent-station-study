package com.ywzai.domain.agent.service.armory;


import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.model.entity.ArmoryCommendEntity;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.model.valobj.AiClientToolMcpVO;
import com.ywzai.domain.agent.service.armory.factory.DefaultArmoryStrategyFactory;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-20
 * @Description: ToolMcp服务装配
 * @Version: 1.0
 */
@Service
@Slf4j
public class AiClientToolMcpNode extends AbstractArmorySupport {


    @Resource
    private AiClientModelNode aiClientModelNode;

    @Override
    protected String doApply(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        List<AiClientToolMcpVO> aiClientToolMcpVOList = dynamicContext.getValue(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName());
        if (aiClientToolMcpVOList == null || aiClientToolMcpVOList.isEmpty()) {
            log.warn("没有需要被初始化的 ai client tool mcp");
            return null;
        }
        for (AiClientToolMcpVO aiClientToolMcpVO : aiClientToolMcpVOList) {
            McpSyncClient mcpSyncClient = armoryToolMcp(aiClientToolMcpVO);
            registerBean(AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getBeanName(aiClientToolMcpVO.getMcpId()), McpSyncClient.class, mcpSyncClient);
        }
        return router(armoryCommendEntity, dynamicContext);
    }

    private McpSyncClient armoryToolMcp(AiClientToolMcpVO aiClientToolMcpVO) {
        String transportType = aiClientToolMcpVO.getTransportType();
        switch (transportType) {
            case "stdio":
                AiClientToolMcpVO.TransportConfigStdio transportConfigStdio = aiClientToolMcpVO.getTransportConfigStdio();
                Map<String, AiClientToolMcpVO.TransportConfigStdio.Stdio> stdioMap = transportConfigStdio.getStdio();
                AiClientToolMcpVO.TransportConfigStdio.Stdio stdio = stdioMap.get(aiClientToolMcpVO.getMcpName());
                var stdioParams = ServerParameters.builder(stdio.getCommand())
                        .args(stdio.getArgs())
                        .env(stdio.getEnv())
                        .build();
                var mcpClient = McpClient.sync(new StdioClientTransport(stdioParams))
                        .requestTimeout(Duration.ofSeconds(aiClientToolMcpVO.getRequestTimeout())).build();
                var initialize = mcpClient.initialize();
                log.info("Stdio Mcp Initialized: {}", initialize);
                return mcpClient;
            case "sse":
                AiClientToolMcpVO.TransportConfigSse transportConfigSse = aiClientToolMcpVO.getTransportConfigSse();
                String baseUri = transportConfigSse.getBaseUri();
                String sseEndpoint = transportConfigSse.getSseEndpoint();
                int indexOf = baseUri.indexOf("sse");
                if(indexOf != -1){
                    sseEndpoint = baseUri.substring(indexOf - 1);
                    baseUri = baseUri.substring(0, indexOf - 1);
                }
                sseEndpoint = StringUtils.isBlank(sseEndpoint) ? "/sse" : sseEndpoint;
                HttpClientSseClientTransport sseClientTransport = HttpClientSseClientTransport.builder(baseUri)
                        .sseEndpoint(sseEndpoint)
                        .build();
                McpSyncClient mcpSyncClient = McpClient.sync(sseClientTransport).requestTimeout(Duration.ofMinutes(aiClientToolMcpVO.getRequestTimeout())).build();
                var init = mcpSyncClient.initialize();
                log.info("SSE MCP Initialized: {}",init);
                return mcpSyncClient;
        }
        throw new RuntimeException("err! transportType " + transportType + " not exist!");
    }

    @Override
    public StrategyHandler<ArmoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext, String> get(ArmoryCommendEntity armoryCommendEntity, DefaultArmoryStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return aiClientModelNode;
    }
}
