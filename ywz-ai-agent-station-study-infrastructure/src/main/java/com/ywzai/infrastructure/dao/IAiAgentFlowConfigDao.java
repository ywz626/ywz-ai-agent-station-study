package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiAgentFlowConfig;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: 智能体流程配置DAO接口
 * @Version: 1.0
 */
@Mapper
public interface IAiAgentFlowConfigDao {

    /**
     * 插入智能体流程配置
     * @param flowConfig 流程配置对象
     * @return 影响行数
     */
    int insert(AiAgentFlowConfig flowConfig);

    /**
     * 更新智能体流程配置
     * @param flowConfig 流程配置对象
     * @return 影响行数
     */
    int update(AiAgentFlowConfig flowConfig);

    /**
     * 根据智能体ID查询流程配置
     * @param agentId 智能体ID
     * @return 流程配置列表
     */
    List<AiAgentFlowConfig> queryByAgentId(Long agentId);

    /**
     * 根据智能体ID和客户端ID查询
     * @param agentId 智能体ID
     * @param clientId 客户端ID
     * @return 流程配置对象
     */
    AiAgentFlowConfig queryByAgentIdAndClientId(Long agentId, Long clientId);

    /**
     * 删除流程配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);
}