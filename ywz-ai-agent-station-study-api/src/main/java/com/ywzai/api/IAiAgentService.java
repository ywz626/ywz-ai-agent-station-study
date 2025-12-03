package com.ywzai.api;

import com.ywzai.api.dto.AiAgentResponseDTO;
import com.ywzai.api.dto.ArmoryAgentRequestDTO;
import com.ywzai.api.dto.AutoAgentRequestDTO;
import com.ywzai.api.response.Response;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

/**
 * @Author: ywz @CreateTime: 2025-09-21 @Description: 对外接口 @Version: 1.0
 */
public interface IAiAgentService {

  /** 装配智能体 */
  Response<Boolean> armoryAgent(ArmoryAgentRequestDTO request);

  /** 查询可用的智能体列表 */
  Response<List<AiAgentResponseDTO>> queryAvailableAgents();

  ResponseBodyEmitter autoAgent(AutoAgentRequestDTO request, HttpServletResponse response);
}
