package com.ywzai.domain.agent.service;

import com.ywzai.domain.agent.model.valobj.AiAgentVO;
import java.util.List;

/**
 * 装配接口
 *
 * @author xiaofuge bugstack.cn @小傅哥 2025/10/3 12:48
 */
public interface IArmoryService {

  List<AiAgentVO> acceptArmoryAllAvailableAgents();

  void acceptArmoryAgent(String agentId);

  List<AiAgentVO> queryAvailableAgents();
}
