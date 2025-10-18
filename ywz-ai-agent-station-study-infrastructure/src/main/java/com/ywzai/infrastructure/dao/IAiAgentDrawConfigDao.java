package com.ywzai.infrastructure.dao;


import com.ywzai.infrastructure.dao.po.AiAgentDrawConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-17
 * @Description: 画布DAO
 * @Version: 1.0
 */
@Mapper
public interface IAiAgentDrawConfigDao {
    AiAgentDrawConfig getByConfigId(String configId);

    int updateByConfigId(AiAgentDrawConfig aiAgentDrawConfig);

    int insert(AiAgentDrawConfig aiAgentDrawConfig);


    int deleteByConfigId(String configId);
}
