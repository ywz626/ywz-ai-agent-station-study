package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiClientModel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: AI客户端模型DAO接口
 * @Version: 1.0
 */
@Mapper
public interface IAiClientModelDao {

    /**
     * 插入模型配置
     * @param model 模型对象
     * @return 影响行数
     */
    int insert(AiClientModel model);

    /**
     * 更新模型配置
     * @param model 模型对象
     * @return 影响行数
     */
    int update(AiClientModel model);

    /**
     * 根据模型ID查询
     * @param modelId 模型ID
     * @return 模型对象
     */
    AiClientModel queryByModelId(String modelId);

    /**
     * 根据API ID查询模型列表
     * @param apiId API ID
     * @return 模型列表
     */
    List<AiClientModel> queryByApiId(String apiId);

    /**
     * 根据模型类型查询
     * @param modelType 模型类型
     * @return 模型列表
     */
    List<AiClientModel> queryByModelType(String modelType);

    /**
     * 查询所有启用的模型
     * @return 模型列表
     */
    List<AiClientModel> queryAllEnabled();

    /**
     * 删除模型
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);


    int updateById(AiClientModel aiClientModel);

    int updateByModelId(AiClientModel aiClientModel);

    int deleteByModelId(String modelId);

    AiClientModel queryById(Long id);

    List<AiClientModel> queryEnabledModels();

    List<AiClientModel> queryAll();
}