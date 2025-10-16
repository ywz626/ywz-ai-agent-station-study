package com.ywzai.trigger.job;


import cn.bugstack.wrench.task.job.model.TaskScheduleVO;
import cn.bugstack.wrench.task.job.provider.ITaskDataProvider;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.model.valobj.AiAgentTaskScheduleVO;
import com.ywzai.domain.agent.service.IAgentDispatchService;
import com.ywzai.domain.agent.service.ITaskService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-16
 * @Description: 组件实现定时任务
 * @Version: 1.0
 */
@Component
@Slf4j
public class AgentTaskJob implements ITaskDataProvider {

    @Resource
    private ITaskService taskService;
    @Resource
    private IAgentDispatchService agentDispatchService;

    /**
     * 查询所有有效的任务调度信息
     *
     * @return 返回所有有效的任务调度信息列表，每个任务包含调度规则、参数和执行逻辑
     */
    @Override
    public List<TaskScheduleVO> queryAllValidTaskSchedule() {
        // 查询所有活跃的任务调度配置
        List<AiAgentTaskScheduleVO> aiAgentTaskScheduleVOS = taskService.queryAllTaskScheduleActivity();
        ArrayList<TaskScheduleVO> taskScheduleVOS = new ArrayList<>();

        // 遍历任务调度配置，转换为统一的任务调度VO对象
        for (AiAgentTaskScheduleVO aiAgentTaskScheduleVO : aiAgentTaskScheduleVOS) {
            TaskScheduleVO taskScheduleVO = new TaskScheduleVO();
            taskScheduleVO.setDescription(aiAgentTaskScheduleVO.getDescription());
            taskScheduleVO.setCronExpression(aiAgentTaskScheduleVO.getCronExpression());
            taskScheduleVO.setTaskParam(aiAgentTaskScheduleVO.getTaskParam());
            taskScheduleVO.setId(aiAgentTaskScheduleVO.getId());

            // 设置任务执行逻辑，通过agentDispatchService分发执行命令
            taskScheduleVO.setTaskLogic(() -> {
                try {
                    agentDispatchService.dispatch(ExecuteCommandEntity.builder()
                                    .aiAgentId(aiAgentTaskScheduleVO.getAgentId())
                                    .sessionId(String.valueOf(System.nanoTime()))
                                    .maxStep(1)
                                    .message(aiAgentTaskScheduleVO.getTaskParam())
                                    .build(),
                            new ResponseBodyEmitter());
                } catch (Exception e) {
                    log.error("定时任务执行异常：{}", e.getMessage(), e);
                }
            });
            taskScheduleVOS.add(taskScheduleVO);
        }
        return taskScheduleVOS;
    }


    @Override
    public List<Long> queryAllInvalidTaskScheduleIds() {
        return taskService.queryAllInvalidTaskScheduleIds();
    }
}
