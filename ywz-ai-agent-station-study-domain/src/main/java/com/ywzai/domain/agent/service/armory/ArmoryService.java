package com.ywzai.domain.agent.service.armory;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.entity.ArmoryCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentClientFlowConfigVO;
import com.ywzai.domain.agent.model.valobj.AiAgentVO;
import com.ywzai.domain.agent.model.valobj.enums.AiAgentEnumVO;
import com.ywzai.domain.agent.service.IArmoryService;
import com.ywzai.domain.agent.service.armory.node.DefaultArmoryStrategyFactory;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * 装配服务
 *
 * @author xiaofuge bugstack.cn @小傅哥 2025/10/3 12:50
 */
@Service
public class ArmoryService implements IArmoryService {

  @Resource private IAgentRepository repository;

  @Resource private DefaultArmoryStrategyFactory defaultArmoryStrategyFactory;

  @Override
  public List<AiAgentVO> acceptArmoryAllAvailableAgents() {
    List<AiAgentVO> aiAgentVOS = repository.queryAvailableAgents();
    for (AiAgentVO aiAgentVO : aiAgentVOS) {
      String agentId = aiAgentVO.getAgentId();
      acceptArmoryAgent(agentId);
    }
    return aiAgentVOS;
  }

  @Override
  public void acceptArmoryAgent(String agentId) {
    List<AiAgentClientFlowConfigVO> aiAgentClientFlowConfigVOS =
        repository.getAiAgentFlowConfigListByAgentId(agentId);
    if (aiAgentClientFlowConfigVOS.isEmpty()) return;

    // 获取客户端集合
    List<String> commandIdList =
        aiAgentClientFlowConfigVOS.stream()
            .map(AiAgentClientFlowConfigVO::getClientId)
            .collect(Collectors.toList());

    try {
      StrategyHandler<ArmoryCommandEntity, DefaultArmoryStrategyFactory.DynamicContext, String>
          armoryStrategyHandler = defaultArmoryStrategyFactory.get();

      armoryStrategyHandler.apply(
          ArmoryCommandEntity.builder()
              .commendType(AiAgentEnumVO.AI_CLIENT.getCode())
              .commendList(commandIdList)
              .build(),
          new DefaultArmoryStrategyFactory.DynamicContext());
    } catch (Exception e) {
      throw new RuntimeException("装配智能体失败", e);
    }
  }

  @Override
  public List<AiAgentVO> queryAvailableAgents() {
    return repository.queryAvailableAgents();
  }
}
