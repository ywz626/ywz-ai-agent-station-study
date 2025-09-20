package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiClientConfig;
import com.ywzai.infrastructure.dao.po.AiClientToolMcp;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: AI客户端配置关联DAO接口
 * @Version: 1.0
 */
@Mapper
public interface IAiClientConfigDao {

    /**
     * 插入客户端配置
     * @param config 配置对象
     * @return 影响行数
     */
    int insert(AiClientConfig config);

    /**
     * 更新客户端配置
     * @param config 配置对象
     * @return 影响行数
     */
    int update(AiClientConfig config);

    /**
     * 根据源ID查询配置
     * @param sourceId 源ID
     * @return 配置列表
     */
    List<AiClientConfig> queryBySourceId(String sourceId);

    /**
     * 根据源类型和源ID查询
     * @param sourceType 源类型
     * @param sourceId 源ID
     * @return 配置列表
     */
    List<AiClientConfig> queryBySourceTypeAndId(String sourceType, String sourceId);

    /**
     * 根据目标类型和目标ID查询
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @return 配置列表
     */
    List<AiClientConfig> queryByTargetTypeAndId(String targetType, String targetId);

    /**
     * 删除配置
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    String queryModelIdByClientId(String clientId);

    List<String> queryPromptIdsByClientId(String clientId);

    List<String> queryAdvisorsByClientId(String clientId);

    List<String> queryToolMcpIdsByModelId(String modelId);
}