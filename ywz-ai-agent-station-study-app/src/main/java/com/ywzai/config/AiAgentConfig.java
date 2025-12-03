package com.ywzai.config;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: Ai配置类 @Version: 1.0
 */
@Configuration
public class AiAgentConfig {

  @Bean("vectorStore")
  public PgVectorStore pgVectorStore(
      @Qualifier("pgVectorJdbcTemplate") JdbcTemplate jdbcTemplate,
      @Qualifier("openAiEmbeddingModel") OpenAiEmbeddingModel embeddingModel) {

    return PgVectorStore.builder(jdbcTemplate, embeddingModel)
        .vectorTableName("vector_store_openai")
        .build();
  }

  @Bean
  public TokenTextSplitter tokenTextSplitter() {
    return new TokenTextSplitter();
  }

  @Bean
  public OpenAiEmbeddingModel openAiEmbeddingModel(
      @Value("${spring.ai.openai.base-url}") String baseUrl,
      @Value("${spring.ai.openai.api-key}") String apiKey,
      @Value("${spring.ai.openai.embedding.options.dimensions}") int dimensions,
      @Value("${spring.ai.openai.embedding.options.model}") String embeddingModel) {
    OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(baseUrl).apiKey(apiKey).build();
    OpenAiEmbeddingOptions options =
        OpenAiEmbeddingOptions.builder().model(embeddingModel).dimensions(dimensions).build();
    return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, options);
  }
}
