package com.ywzai.domain.agent.model.valobj.enums;


import com.ywzai.domain.agent.model.valobj.AiClientAdvisorVO;
import com.ywzai.domain.agent.service.armory.node.factory.element.RagAnswerAdvisor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-20
 * @Description: 顾问类型枚举
 * @Version: 1.0
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum AiClientAdvisorTypeEnumVO {

    CHAT_MEMORY("ChatMemory", "记忆上下文(内存模式)") {
        @Override
        public Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore) {
            AiClientAdvisorVO.ChatMemory chatMemory = aiClientAdvisorVO.getChatMemory();
            int maxMessages = chatMemory.getMaxMessages();
            return PromptChatMemoryAdvisor.builder(MessageWindowChatMemory.builder()
                    .maxMessages(maxMessages)
                    .build()).build();
        }
    },
    RAG_ANSWER("RagAnswer", "知识库") {
        @Override
        public Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore) {
            AiClientAdvisorVO.RagAnswer ragAnswer = aiClientAdvisorVO.getRagAnswer();
            int topK = ragAnswer.getTopK();
            String filterExpression = ragAnswer.getFilterExpression();
            return new RagAnswerAdvisor(vectorStore, SearchRequest.builder()
                    .topK(topK)
                    .filterExpression(filterExpression)
                    .build()
            );
        }
    };


    private String code;
    private String description;

    private static final Map<String, AiClientAdvisorTypeEnumVO> CODE_MAP = new HashMap<>();

    static {
        for (AiClientAdvisorTypeEnumVO value : AiClientAdvisorTypeEnumVO.values()) {
            CODE_MAP.put(value.code, value);
        }
    }

    public static AiClientAdvisorTypeEnumVO getByCode(String code) {
        AiClientAdvisorTypeEnumVO enumVO = CODE_MAP.get(code);
        if (enumVO == null) {
            throw new RuntimeException("err! advisorType " + code + " not exist!");
        }
        return enumVO;
    }

    public abstract Advisor createAdvisor(AiClientAdvisorVO aiClientAdvisorVO, VectorStore vectorStore);

}
