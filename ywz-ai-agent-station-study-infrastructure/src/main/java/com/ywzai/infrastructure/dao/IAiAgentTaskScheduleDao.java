package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiAgentTaskSchedule;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: 智能体任务调度DAO接口 @Version: 1.0
 */
@Mapper
public interface IAiAgentTaskScheduleDao {

  /**
   * 插入任务调度配置
   *
   * @param taskSchedule 任务调度对象
   * @return 影响行数
   */
  int insert(AiAgentTaskSchedule taskSchedule);

  /**
   * 更新任务调度配置
   *
   * @param taskSchedule 任务调度对象
   * @return 影响行数
   */
  int update(AiAgentTaskSchedule taskSchedule);

  /**
   * 根据智能体ID查询任务调度
   *
   * @param agentId 智能体ID
   * @return 任务调度列表
   */
  List<AiAgentTaskSchedule> queryByAgentId(String agentId);

  /**
   * 查询所有启用的任务调度
   *
   * @return 任务调度列表
   */
  List<AiAgentTaskSchedule> queryAllEnabled();

  /**
   * 根据ID查询
   *
   * @param id 主键ID
   * @return 任务调度对象
   */
  AiAgentTaskSchedule queryById(Long id);

  /**
   * 删除任务调度
   *
   * @param id 主键ID
   * @return 影响行数
   */
  int deleteById(Long id);

  List<AiAgentTaskSchedule> queryActivity();

  List<Long> queryInvalid();
}
