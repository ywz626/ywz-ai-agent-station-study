package com.ywzai.config;


import cn.bugstack.wrench.task.job.TaskJob;
import cn.bugstack.wrench.task.job.config.TaskJobAutoProperties;
import cn.bugstack.wrench.task.job.provider.ITaskDataProvider;
import cn.bugstack.wrench.task.job.service.ITaskJobService;
import cn.bugstack.wrench.task.job.service.TaskJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-16
 * @Description: 定时任务配置
 * @Version: 1.0
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties({TaskJobAutoProperties.class})
@ConditionalOnProperty(
        prefix = "xfg.wrench.task.job",
        name = {"enabled"},
        havingValue = "true",
        matchIfMissing = true
)
@Slf4j
public class TaskJobAutoConfigBean {


    @Bean({"xfgWrenchTaskScheduler"})
    public TaskScheduler taskScheduler(TaskJobAutoProperties properties) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(properties.getPoolSize());
        scheduler.setThreadNamePrefix(properties.getThreadNamePrefix());
        scheduler.setWaitForTasksToCompleteOnShutdown(properties.isWaitForTasksToCompleteOnShutdown());
        scheduler.setAwaitTerminationSeconds(properties.getAwaitTerminationSeconds());
        scheduler.initialize();
        log.info("xfg-wrench，任务调度器初始化完成。线程池大小: {}, 线程名前缀: {}", properties.getPoolSize(), properties.getThreadNamePrefix());
        return scheduler;
    }

    @Bean
    public ITaskJobService taskJobService(TaskScheduler xfgWrenchTaskScheduler, List<ITaskDataProvider> taskDataProviders) {
        TaskJobService taskJobService = new TaskJobService(xfgWrenchTaskScheduler, taskDataProviders);
        taskJobService.initializeTasks();
        return taskJobService;
    }

    @Bean
    public TaskJob taskJob(TaskJobAutoProperties properties, ITaskJobService taskJobService) {
        log.info("xfg-wrench，任务调度作业初始化完成。刷新间隔: {}ms, 清理cron: {}", properties.getRefreshInterval(), properties.getCleanInvalidTasksCron());
        return new TaskJob(properties, taskJobService);
    }

}
