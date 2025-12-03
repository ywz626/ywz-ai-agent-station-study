package com.ywzai.domain.agent.service;

import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @Author: ywz @CreateTime: 2025-10-16 @Description: 策略模式调度器 @Version: 1.0
 */
public interface IAgentDispatchService {
  void dispatch(ExecuteCommandEntity requestParameter, ResponseBodyEmitter emitter)
      throws Exception;
}
