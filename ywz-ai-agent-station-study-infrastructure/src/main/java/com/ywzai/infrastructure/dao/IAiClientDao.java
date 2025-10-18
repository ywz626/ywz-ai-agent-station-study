package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiClient;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: AI客户端DAO接口
 * @Version: 1.0
 */
@Mapper
public interface IAiClientDao {

    /**
     * 插入AI客户端
     * @param aiClient AI客户端对象
     * @return 影响行数
     */
    int insert(AiClient aiClient);

    /**
     * 更新AI客户端
     * @param aiClient AI客户端对象
     * @return 影响行数
     */
    int update(AiClient aiClient);

    /**
     * 根据客户端ID查询
     * @param clientId 客户端ID
     * @return AI客户端对象
     */
    AiClient queryByClientId(String clientId);

    /**
     * 根据ID查询
     * @param id 主键ID
     * @return AI客户端对象
     */
    AiClient queryById(Long id);

    /**
     * 查询所有启用的客户端
     * @return AI客户端列表
     */
    List<AiClient> queryAllEnabled();

    /**
     * 删除客户端
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    List<AiClient> queryAll();

    List<AiClient> queryByClientName(String clientName);

    List<AiClient> queryEnabledClients();

    int deleteByClientId(String clientId);

    int updateByClientId(AiClient aiClient);

    int updateById(AiClient aiClient);
}