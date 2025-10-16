package com.ywzai.test;

import com.ywzai.infrastructure.dao.*;
import com.ywzai.infrastructure.dao.po.*;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: DAO层简单测试
 * @Version: 1.0
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DaoTest {

    @Resource
    private IAiAgentDao aiAgentDao;

    @Resource
    private IAiAgentFlowConfigDao aiAgentFlowConfigDao;

    @Resource
    private IAiAgentTaskScheduleDao aiAgentTaskScheduleDao;

    @Resource
    private IAiClientDao aiClientDao;

    @Resource
    private IAiClientAdvisorDao aiClientAdvisorDao;

    @Resource
    private IAiClientApiDao aiClientApiDao;

    @Resource
    private IAiClientConfigDao aiClientConfigDao;

    @Resource
    private IAiClientModelDao aiClientModelDao;

    @Resource
    private IAiClientRagOrderDao aiClientRagOrderDao;

    @Resource
    private IAiClientSystemPromptDao aiClientSystemPromptDao;

    @Resource
    private IAiClientToolMcpDao aiClientToolMcpDao;

    @Test
    public void test_ai_agent_dao() {
        // 测试查询智能体
        AiAgent aiAgent = aiAgentDao.queryByAgentId("1");
        log.info("查询智能体结果: {}", aiAgent);

        // 测试查询所有启用的智能体
        List<AiAgent> aiAgents = aiAgentDao.queryAllEnabled();
        log.info("查询所有启用智能体结果: {}", aiAgents);
    }



    @Test
    public void test_ai_agent_task_schedule_dao() {
        // 测试查询所有启用的任务调度
        List<AiAgentTaskSchedule> taskSchedules = aiAgentTaskScheduleDao.queryAllEnabled();
        log.info("查询所有启用任务调度结果: {}", taskSchedules);

        // 测试根据智能体ID查询任务调度
        List<AiAgentTaskSchedule> agentTaskSchedules = aiAgentTaskScheduleDao.queryByAgentId("1");
        log.info("查询智能体任务调度结果: {}", agentTaskSchedules);
    }

    @Test
    public void test_ai_client_dao() {
        // 测试根据客户端ID查询
        AiClient aiClient = aiClientDao.queryByClientId("3001");
        log.info("查询AI客户端结果: {}", aiClient);

        // 测试查询所有启用的客户端
        List<AiClient> aiClients = aiClientDao.queryAllEnabled();
        log.info("查询所有启用AI客户端结果: {}", aiClients);
    }

    @Test
    public void test_ai_client_advisor_dao() {
        // 测试根据顾问ID查询
        AiClientAdvisor advisor = aiClientAdvisorDao.queryByAdvisorId("4001");
        log.info("查询AI客户端顾问结果: {}", advisor);

        // 测试查询所有启用的顾问
        List<AiClientAdvisor> advisors = aiClientAdvisorDao.queryAllEnabled();
        log.info("查询所有启用顾问结果: {}", advisors);
    }

    @Test
    public void test_ai_client_api_dao() {
        // 测试根据API ID查询
        AiClientApi apiConfig = aiClientApiDao.queryByApiId("1001");
        log.info("查询AI客户端API配置结果: {}", apiConfig);

        // 测试查询所有启用的API配置
        List<AiClientApi> apiConfigs = aiClientApiDao.queryAllEnabled();
        log.info("查询所有启用API配置结果: {}", apiConfigs);
    }

    @Test
    public void test_ai_client_config_dao() {
        // 测试根据源ID查询配置
        List<AiClientConfig> configs = aiClientConfigDao.queryBySourceId("3001");
        log.info("查询AI客户端配置结果: {}", configs);

        // 测试根据源类型和源ID查询
        List<AiClientConfig> configsByType = aiClientConfigDao.queryBySourceTypeAndId("client", "3001");
        log.info("查询AI客户端配置(按类型)结果: {}", configsByType);
    }

    @Test
    public void test_ai_client_model_dao() {
        // 测试根据模型ID查询
        AiClientModel model = aiClientModelDao.queryByModelId("2001");
        log.info("查询AI客户端模型结果: {}", model);

        // 测试查询所有启用的模型
        List<AiClientModel> models = aiClientModelDao.queryAllEnabled();
        log.info("查询所有启用模型结果: {}", models);
    }

    @Test
    public void test_ai_client_rag_order_dao() {
        // 测试根据RAG ID查询
        AiClientRagOrder ragOrder = aiClientRagOrderDao.queryByRagId("9001");
        log.info("查询AI客户端RAG配置结果: {}", ragOrder);

        // 测试查询所有启用的RAG配置
        List<AiClientRagOrder> ragOrders = aiClientRagOrderDao.queryAllEnabled();
        log.info("查询所有启用RAG配置结果: {}", ragOrders);
    }

    @Test
    public void test_ai_client_system_prompt_dao() {
        // 测试根据提示词ID查询
        AiClientSystemPrompt prompt = aiClientSystemPromptDao.queryByPromptId("6001");
        log.info("查询AI客户端系统提示词结果: {}", prompt);

        // 测试查询所有启用的系统提示词
        List<AiClientSystemPrompt> prompts = aiClientSystemPromptDao.queryAllEnabled();
        log.info("查询所有启用系统提示词结果: {}", prompts);
    }

    @Test
    public void test_ai_client_tool_mcp_dao() {
        // 测试根据MCP ID查询
        AiClientToolMcp toolMcp = aiClientToolMcpDao.queryByMcpId("5001");
        log.info("查询AI客户端MCP工具结果: {}", toolMcp);

        // 测试查询所有启用的MCP工具
        List<AiClientToolMcp> toolMcps = aiClientToolMcpDao.queryAllEnabled();
        log.info("查询所有启用MCP工具结果: {}", toolMcps);

        // 测试根据传输类型查询
        List<AiClientToolMcp> sseTools = aiClientToolMcpDao.queryByTransportType("sse");
        log.info("查询SSE类型MCP工具结果: {}", sseTools);
    }

    @Test
    public void test_insert_ai_agent() {
        // 测试插入新的AI智能体
        AiAgent newAgent = AiAgent.builder()
                .agentId("test-001")
                .agentName("测试智能体")
                .description("这是一个测试智能体")
                .channel("test")
                .status(1)
                .build();

        int result = aiAgentDao.insert(newAgent);
        log.info("插入AI智能体结果: {}", result);
    }

    @Test
    public void test_insert_ai_client() {
        // 测试插入新的AI客户端
        AiClient newClient = AiClient.builder()
                .clientId("test-client-001")
                .clientName("测试客户端")
                .description("这是一个测试客户端")
                .status(1)
                .build();

        int result = aiClientDao.insert(newClient);
        log.info("插入AI客户端结果: {}", result);
    }
}