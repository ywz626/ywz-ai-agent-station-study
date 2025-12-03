package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiClientRagOrder;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: AI客户端RAG配置DAO接口 @Version: 1.0
 */
@Mapper
public interface IAiClientRagOrderDao {

  /**
   * 插入RAG配置
   *
   * @param ragOrder RAG配置对象
   * @return 影响行数
   */
  int insert(AiClientRagOrder ragOrder);

  /**
   * 更新RAG配置
   *
   * @param ragOrder RAG配置对象
   * @return 影响行数
   */
  int update(AiClientRagOrder ragOrder);

  /**
   * 根据RAG ID查询
   *
   * @param ragId RAG ID
   * @return RAG配置对象
   */
  AiClientRagOrder queryByRagId(String ragId);

  /**
   * 根据知识标签查询
   *
   * @param knowledgeTag 知识标签
   * @return RAG配置列表
   */
  List<AiClientRagOrder> queryByKnowledgeTag(String knowledgeTag);

  /**
   * 查询所有启用的RAG配置
   *
   * @return RAG配置列表
   */
  List<AiClientRagOrder> queryAllEnabled();

  /**
   * 删除RAG配置
   *
   * @param id 主键ID
   * @return 影响行数
   */
  int deleteById(Long id);

  List<AiClientRagOrder> queryAll();

  int updateById(AiClientRagOrder aiClientRagOrder);

  int updateByRagId(AiClientRagOrder aiClientRagOrder);

  int deleteByRagId(String ragId);

  AiClientRagOrder queryById(Long id);

  List<AiClientRagOrder> queryEnabledRagOrders();
}
