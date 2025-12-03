package com.ywzai.domain.agent.service.armory.business.data.impl;

import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import com.ywzai.domain.agent.model.valobj.*;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.service.armory.business.data.ILoadDataStrategy;
import com.ywzai.domain.agent.service.armory.node.DefaultArmoryStrategyFactory;
import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: ywz @CreateTime: 2025-09-19 @Description: Client类型加载数据 @Version: 1.0
 */
@Service
@Slf4j
public class AiClientLoadDataStrategy implements ILoadDataStrategy {

  @Resource protected ThreadPoolExecutor threadPoolExecutor;
  @Resource private IAgentRepository agentRepository;

  /**
   * 加载与指定 ArmoryCommandEntity 关联的多种 AI 客户端相关配置数据，并将结果存储到 DynamicContext 中。
   *
   * <p>该方法会根据 commendList 中的 clientIds 异步查询以下几类配置数据： - AiClientApiVO（客户端API配置） -
   * AiClientModelVO（客户端模型配置） - AiClientPromptVO（客户端提示词配置） - AiClientAdvisorVO（客户端顾问配置） -
   * AiClientToolMcpVO（客户端工具 MCP 配置） - AiClientVO（客户端基础信息）
   *
   * <p>所有查询操作使用线程池并发执行，若任一查询失败则记录错误日志并返回空集合或空映射作为默认值。 最终将所有查询结果设置进 dynamicContext。
   *
   * @param armoryCommandEntity 命令实体，包含需要加载配置的客户端ID列表（commendList）
   * @param dynamicContext 动态上下文对象，用于存储加载后的配置数据
   */
  @Override
  public void loadData(
      ArmoryCommandEntity armoryCommandEntity,
      DefaultArmoryStrategyFactory.DynamicContext dynamicContext) {
    List<String> clientIds = armoryCommandEntity.getCommendList();
    if (clientIds == null || clientIds.isEmpty()) {
      log.debug("clientIds为空直接返回");
      return;
    }

    // 异步查询各类型配置数据
    CompletableFuture<List<AiClientApiVO>> apiFuture =
        CompletableFuture.supplyAsync(
                () -> {
                  log.info("查询配置数据(ai_client_api) {}", clientIds);
                  return agentRepository.getAiClientApiVOListByClientIds(clientIds);
                },
                threadPoolExecutor)
            .exceptionally(
                ex -> {
                  log.error("查询配置数据(ai_client_api) {} 异常", clientIds, ex);
                  return List.of();
                });

    CompletableFuture<List<AiClientModelVO>> modelFuture =
        CompletableFuture.supplyAsync(
                () -> {
                  log.info("查询配置数据(ai_client_model) {}", clientIds);
                  return agentRepository.getAiClientModelVOListByClientIds(clientIds);
                },
                threadPoolExecutor)
            .exceptionally(
                ex -> {
                  log.error("查询配置数据(ai_client_model) {} 异常", clientIds, ex);
                  return List.of();
                });

    CompletableFuture<Map<String, AiClientPromptVO>> promptFuture =
        CompletableFuture.supplyAsync(
                () -> {
                  log.info("查询配置数据(ai_client_prompt) {}", clientIds);
                  return agentRepository.getAiClientPromptVOListByClientIds(clientIds);
                },
                threadPoolExecutor)
            .exceptionally(
                ex -> {
                  log.error("查询配置数据(ai_client_prompt) {} 异常", clientIds, ex);
                  return Map.of();
                });

    CompletableFuture<List<AiClientAdvisorVO>> advisorFuture =
        CompletableFuture.supplyAsync(
                () -> {
                  log.info("查询配置数据(ai_client_advisor) {}", clientIds);
                  return agentRepository.getAiClientAdvisorVOListByClientIds(clientIds);
                },
                threadPoolExecutor)
            .exceptionally(
                ex -> {
                  log.error("查询配置数据(ai_client_advisor) {} 异常", clientIds, ex);
                  return List.of();
                });

    CompletableFuture<List<AiClientToolMcpVO>> toolMcpFuture =
        CompletableFuture.supplyAsync(
                () -> {
                  log.info("查询配置数据(ai_client_tool_mcp) {}", clientIds);
                  return agentRepository.getAiClientToolMcpVOListByClientIds(clientIds);
                },
                threadPoolExecutor)
            .exceptionally(
                ex -> {
                  log.error("查询配置数据(ai_client_tool_mcp) {} 异常", clientIds, ex);
                  return List.of();
                });

    CompletableFuture<List<AiClientVO>> clientFuture =
        CompletableFuture.supplyAsync(
                () -> {
                  log.info("查询配置数据(ai_client) {}", clientIds);
                  return agentRepository.getAiClientVOListByClientIds(clientIds);
                },
                threadPoolExecutor)
            .exceptionally(
                ex -> {
                  log.error("查询配置数据(ai_client) {} 异常", clientIds, ex);
                  return List.of();
                });

    // 等待所有异步任务完成
    CompletableFuture<Void> allFutures =
        CompletableFuture.allOf(
            apiFuture, modelFuture, promptFuture, advisorFuture, toolMcpFuture, clientFuture);

    // 处理所有查询结果，统一设置到上下文中
    allFutures
        .handle(
            (unused, throwable) -> {
              if (throwable != null) {
                log.error("异步查询配置数据时发生异常", throwable);
                // 可以设置默认值或抛出业务异常
                throw new RuntimeException("查询配置失败", throwable);
              }

              // 所有 future 成功完成，安全获取结果
              dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT_API.getDataName(), apiFuture.join());
              dynamicContext.setValue(
                  AiAgentEnumVO.AI_CLIENT_MODEL.getDataName(), modelFuture.join());
              dynamicContext.setValue(
                  AiAgentEnumVO.AI_CLIENT_SYSTEM_PROMPT.getDataName(), promptFuture.join());
              dynamicContext.setValue(
                  AiAgentEnumVO.AI_CLIENT_TOOL_MCP.getDataName(), toolMcpFuture.join());
              dynamicContext.setValue(
                  AiAgentEnumVO.AI_CLIENT_ADVISOR.getDataName(), advisorFuture.join());
              dynamicContext.setValue(AiAgentEnumVO.AI_CLIENT.getDataName(), clientFuture.join());

              return null;
            })
        .join(); // 等待处理完成
  }
}
