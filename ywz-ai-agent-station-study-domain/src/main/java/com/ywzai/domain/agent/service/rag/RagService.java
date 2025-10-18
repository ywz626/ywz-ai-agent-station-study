package com.ywzai.domain.agent.service.rag;


import com.ywzai.domain.agent.adapter.repository.IAgentRepository;
import com.ywzai.domain.agent.model.valobj.AiRagOrderVO;
import com.ywzai.domain.agent.service.IRagService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库服务
 * @author xiaofuge bugstack.cn @小傅哥
 * 2025/10/4 09:12
 */
@Slf4j
@Service
public class RagService implements IRagService {

    @Resource
    private TokenTextSplitter tokenTextSplitter;

    @Resource
    private PgVectorStore vectorStore;

    @Resource
    private IAgentRepository repository;

    @Override
    public void storeRagFile(String name, String tag, List<MultipartFile> files) {
        for (MultipartFile file : files) {
            TikaDocumentReader documentReader = new TikaDocumentReader(file.getResource());
            List<Document> documentList = tokenTextSplitter.apply(documentReader.get());

            // 添加知识库标签
            documentList.forEach(doc -> doc.getMetadata().put("knowledge", tag));

            // 存储知识库文件
            vectorStore.accept(documentList);

            // 存储到数据库
            AiRagOrderVO aiRagOrderVO = new AiRagOrderVO();
            aiRagOrderVO.setRagName(name);
            aiRagOrderVO.setKnowledgeTag(tag);
            repository.createTagOrder(aiRagOrderVO);
        }
    }

}
