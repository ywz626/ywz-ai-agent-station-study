package com.ywzai.domain.agent.service.armory.node.factory.element;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RagAnswerAdvisor implements BaseAdvisor {

    private final VectorStore vectorStore;
    private final SearchRequest searchRequest;
    private final String userTextAdvise;

    public RagAnswerAdvisor(VectorStore vectorStore, SearchRequest searchRequest) {
        this.vectorStore = vectorStore;
        this.searchRequest = searchRequest;
        this.userTextAdvise = "\nContext information is below, surrounded by ---------------------\n\n---------------------\n{question_answer_context}\n---------------------\n\nGiven the context and provided history information and not prior knowledge,\nreply to the user comment. If the answer is not in the context, inform\nthe user that you can't answer the question.\n";
    }

    /**
     * 在处理聊天请求之前，先进行文档检索并构建带有上下文的用户消息
     *
     * @param chatClientRequest 原始聊天客户端请求，包含用户消息和上下文信息
     * @param advisorChain      顾问链，用于处理请求的后续流程
     * @return 构建后的新聊天客户端请求，包含检索到的文档上下文和增强后的用户消息
     */
    @Override
    public ChatClientRequest before(ChatClientRequest chatClientRequest, AdvisorChain advisorChain) {
        // 创建新的上下文映射，用于存储检索到的文档等信息
        HashMap<String, Object> context = new HashMap<>(chatClientRequest.context());
        // 获取用户原始消息文本
        String userText = chatClientRequest.prompt().getUserMessage().getText();
        // 构建搜索请求，基于用户消息文本和过滤表达式进行相似性搜索
        SearchRequest searchRequestToUse = SearchRequest.from(this.searchRequest).query(userText).filterExpression(this.doGetFilterExpression(context)).build();

        // 执行向量存储的相似性搜索，获取相关文档
        List<Document> documents = this.vectorStore.similaritySearch(searchRequestToUse);

        // 将检索到的文档存入上下文
        context.put("qa_retrieved_documents", documents);
        // 将所有检索到的文档内容合并为一个字符串上下文
        String documentContext = documents.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));

        // 创建用于模板渲染的参数映射，包含原始上下文和文档上下文
        Map<String, Object> advisedUserParams = new HashMap<>(context);
        advisedUserParams.put("question_answer_context", documentContext);

        // 使用模板渲染增强后的用户消息文本
        String advisedUserText = new PromptTemplate(this.userTextAdvise)
                .render(advisedUserParams);
        // 构建并返回新的聊天客户端请求，包含增强后的用户消息和更新的上下文
        return ChatClientRequest.builder()
                .prompt(Prompt.builder().messages(new UserMessage(advisedUserText), new AssistantMessage(JSON.toJSONString(advisedUserParams))).build())
                .context(advisedUserParams)
                .build();
    }

    /**
     * 在聊天客户端响应处理链中的后置处理方法
     *
     * @param chatClientResponse 聊天客户端响应对象，包含原始的聊天响应和上下文信息
     * @param advisorChain       处理链对象，用于继续执行后续的处理器
     * @return 经过处理后的聊天客户端响应对象
     */
    @Override
    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        // 构建新的聊天响应对象，复制原始响应的基础信息
        ChatResponse.Builder chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
        // 将QA检索到的文档信息从上下文中提取并设置到响应的元数据中
        chatResponseBuilder.metadata("qa_retrieved_documents", chatClientResponse.context().get("qa_retrieved_documents"));
        ChatResponse chatResponse = chatResponseBuilder.build();

        // 构建并返回新的聊天客户端响应对象
        return ChatClientResponse.builder()
                .chatResponse(chatResponse)
                .context(chatClientResponse.context())
                .build();
    }


    /**
     * 处理聊天客户端请求并生成响应
     *
     * @param chatClientRequest 聊天客户端请求对象，包含上下文信息
     * @param callAdvisorChain  调用顾问链，用于处理请求的链式调用
     * @return ChatClientResponse 聊天客户端响应对象
     */
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        // 检查并处理上下文中的结束时间标签
        Map<String, Object> context = chatClientRequest.context();
        if (context.containsKey("end time")) {
            log.info("找到end time 标签");
            Object endTime = context.get("end time");
            if (endTime == null || endTime.toString().trim().isEmpty()) {
                context.put("end time", "now");
            }
        }

        // 执行链式调用处理请求并返回响应
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(this.before(chatClientRequest, callAdvisorChain));
        return this.after(chatClientResponse, callAdvisorChain);
    }


    /**
     * 流式处理聊天客户端请求并返回响应流
     *
     * @param chatClientRequest  聊天客户端请求对象，包含用户输入和相关上下文信息
     * @param streamAdvisorChain 流式顾问链，用于处理流式响应的链式调用
     * @return Flux<ChatClientResponse> 响应流，包含聊天机器人的回复内容
     */
    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        return BaseAdvisor.super.adviseStream(chatClientRequest, streamAdvisorChain);
    }


    @Override
    public int getOrder() {
        return 0;
    }

    /**
     * 获取当前对象的类名
     *
     * @return 返回当前类的简单名称（不包含包名的类名）
     */
    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }


    /**
     * 获取过滤表达式
     *
     * @param context 上下文参数映射表，用于获取过滤表达式配置
     * @return 返回解析后的过滤表达式对象，如果上下文中没有有效的过滤表达式则返回默认的搜索请求过滤表达式
     */
    protected Filter.Expression doGetFilterExpression(Map<String, Object> context) {
        // 检查上下文中是否包含qa_filter_expression键，且其值不为空字符串
        // 如果条件满足，则解析上下文中的过滤表达式文本；否则使用默认的搜索请求过滤表达式
        return context.containsKey("qa_filter_expression") && StringUtils.hasText(context.get("qa_filter_expression").toString()) ? (new FilterExpressionTextParser()).parse(context.get("qa_filter_expression").toString()) : this.searchRequest.getFilterExpression();
    }


}
