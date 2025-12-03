package com.ywzai.domain.agent.service.task;

import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.valobj.AiAgentTaskScheduleVO;
import com.ywzai.domain.agent.service.ITaskService;
import jakarta.annotation.Resource;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Author: ywz @CreateTime: 2025-10-16 @Description: 定时任务服务 @Version: 1.0
 */
@Service
@Slf4j
public class TaskService implements ITaskService {

  @Resource private IAgentRepository repository;

  @Override
  public List<AiAgentTaskScheduleVO> queryAllTaskScheduleActivity() {
    return repository.queryAllActivityTaskSchedule();
  }

  @Override
  public List<Long> queryAllInvalidTaskScheduleIds() {
    return repository.queryAllInvalidTaskSchedule();
  }
}
