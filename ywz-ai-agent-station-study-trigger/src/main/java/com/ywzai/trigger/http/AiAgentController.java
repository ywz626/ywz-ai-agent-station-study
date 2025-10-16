package com.ywzai.trigger.http;


import com.alibaba.fastjson.JSON;
import com.ywzai.api.IAiAgentService;
import com.ywzai.api.dto.AutoAgentRequestDTO;
import com.ywzai.domain.agent.model.entity.ExecuteCommandEntity;
import com.ywzai.domain.agent.service.IAgentDispatchService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

import javax.annotation.Resource;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-21
 * @Description: 对外接口
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/agent")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS})
public class AiAgentController implements IAiAgentService {

//    @Resource(name = "autoAgentExecuteStrategy")
//    private IExecuteStrategy autoAgentExecuteStrategy;

    @Resource
    private IAgentDispatchService agentDispatchService;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 自动代理（AutoAgent）流式执行接口。
     * <p>
     * 接收客户端发送的自动代理请求，通过异步方式调度并执行对应的 AI Agent，
     * 并以 Server-Sent Events (SSE) 的形式向客户端实时推送执行结果。
     *
     * @param request  客户端请求参数封装对象，包含AI代理ID、消息内容、会话ID及最大执行步骤等信息
     * @param response HTTP响应对象，用于设置SSE相关响应头
     * @return ResponseBodyEmitter 流式响应输出对象，用于持续向客户端推送数据
     */
    @Override
    @RequestMapping(value = "auto_agent", method = RequestMethod.POST)
    public ResponseBodyEmitter autoAgent(@RequestBody AutoAgentRequestDTO request, HttpServletResponse response) {
        log.info("AutoAgent流式执行请求开始，请求信息：{}", JSON.toJSONString(request));

        try {
            // 设置SSE所需的响应头信息，确保浏览器能正确解析事件流
            response.setContentType("text/event-stream");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Connection", "keep-alive");

            // 1. 创建一个无超时时间的流式响应发射器
            ResponseBodyEmitter emitter = new ResponseBodyEmitter(Long.MAX_VALUE);

            // 2. 将请求参数转换为命令执行实体，便于后续服务调用
            ExecuteCommandEntity executeCommandEntity = ExecuteCommandEntity.builder()
                    .aiAgentId(request.getAiAgentId())
                    .message(request.getMessage())
                    .sessionId(request.getSessionId())
                    .maxStep(request.getMaxStep())
                    .build();

            // 3. 提交任务至线程池进行异步处理，避免阻塞主线程
            agentDispatchService.dispatch(executeCommandEntity, emitter);
            return emitter;

        } catch (Exception e) {
            log.error("AutoAgent请求处理异常：{}", e.getMessage(), e);
            // 出现异常时构造错误响应流
            ResponseBodyEmitter errorEmitter = new ResponseBodyEmitter();
            try {
                errorEmitter.send("请求处理异常：" + e.getMessage());
                errorEmitter.complete();
            } catch (Exception ex) {
                log.error("发送错误信息失败：{}", ex.getMessage(), ex);
            }
            return errorEmitter;
        }
    }

}


