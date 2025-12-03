package com.ywzai.domain.agent.service.dispatch;

import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentVO;
import com.ywzai.domain.agent.service.IAgentDispatchService;
import com.ywzai.domain.agent.service.IExecuteStrategy;
import jakarta.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @Author: ywz @CreateTime: 2025-10-16 @Description: 策略模式调度器实现类 @Version: 1.0
 */
@Service
@Slf4j
public class AgentDispatchService implements IAgentDispatchService {

  @Resource private Map<String, IExecuteStrategy> executeStrategyMap;
  @Resource private ThreadPoolExecutor threadPoolExecutor;
  @Resource private IAgentRepository repository;

  /**
   * 分发执行命令请求到对应的AI代理执行策略
   *
   * @param requestParameter 执行命令实体，包含AI代理ID等执行参数
   * @param emitter 响应体发射器，用于流式输出执行结果
   * @throws Exception 执行过程中可能抛出的异常
   */
  @Override
  public void dispatch(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter)
      throws Exception {
    // 根据AI代理ID获取代理配置信息
    AiAgentVO aiAgentConfigByAgentId =
        repository.getAiAgentConfigByAgentId(requestParameter.getAiAgentId());
    String strategy = aiAgentConfigByAgentId.getStrategy();
    IExecuteStrategy executeStrategy = executeStrategyMap.get(strategy);
    if (null == executeStrategy) {
      throw new RuntimeException("不存在的执行策略: " + strategy);
    }
    // 在线程池中异步执行策略逻辑
    threadPoolExecutor.execute(
        () -> {
          try {
            executeStrategy.execute(requestParameter, emitter);
          } catch (Exception e) {
            log.error("执行异常：{}", e.getMessage(), e);
            try {
              emitter.send("执行异常：" + e.getMessage());
            } catch (Exception ex) {
              log.error("发送异常信息失败：{}", ex.getMessage(), ex);
            }
          } finally {
            try {
              // 完成流式输出
              emitter.complete();
            } catch (Exception e) {
              log.error("完成流式输出失败：{}", e.getMessage(), e);
            }
          }
        });
  }
}
