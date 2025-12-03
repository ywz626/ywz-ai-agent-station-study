package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiAgent;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: AI智能体DAO接口 @Version: 1.0
 */
@Mapper
public interface IAiAgentDao {

  /**
   * 插入AI智能体
   *
   * @param aiAgent AI智能体对象
   * @return 影响行数
   */
  int insert(AiAgent aiAgent);

  /**
   * 更新AI智能体
   *
   * @param aiAgent AI智能体对象
   * @return 影响行数
   */
  int update(AiAgent aiAgent);

  /**
   * 根据智能体ID查询
   *
   * @param agentId 智能体ID
   * @return AI智能体对象
   */
  AiAgent queryByAgentId(String agentId);

  /**
   * 根据ID查询
   *
   * @param id 主键ID
   * @return AI智能体对象
   */
  AiAgent queryById(Long id);

  /**
   * 查询所有启用的智能体
   *
   * @return AI智能体列表
   */
  List<AiAgent> queryAllEnabled();

  /**
   * 删除智能体
   *
   * @param id 主键ID
   * @return 影响行数
   */
  int deleteById(Long id);

  int deleteByAgentId(String agentId);

  List<AiAgent> queryEnabledAgents();

  List<AiAgent> queryAll();
}
