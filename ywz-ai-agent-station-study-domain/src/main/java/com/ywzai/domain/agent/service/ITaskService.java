package com.ywzai.domain.agent.service;

import com.ywzai.domain.agent.model.valobj.AiAgentTaskScheduleVO;
import java.util.List;

public interface ITaskService {

  List<AiAgentTaskScheduleVO> queryAllTaskScheduleActivity();

  List<Long> queryAllInvalidTaskScheduleIds();
}
