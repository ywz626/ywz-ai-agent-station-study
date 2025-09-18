package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiClientAdvisor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: AI客户端顾问DAO接口
 * @Version: 1.0
 */
@Mapper
public interface IAiClientAdvisorDao {

    /**
     * 插入顾问配置
     * @param advisor 顾问对象
     * @return 影响行数
     */
    int insert(AiClientAdvisor advisor);

    /**
     * 更新顾问配置
     * @param advisor 顾问对象
     * @return 影响行数
     */
    int update(AiClientAdvisor advisor);

    /**
     * 根据顾问ID查询
     * @param advisorId 顾问ID
     * @return 顾问对象
     */
    AiClientAdvisor queryByAdvisorId(String advisorId);

    /**
     * 根据顾问类型查询
     * @param advisorType 顾问类型
     * @return 顾问列表
     */
    List<AiClientAdvisor> queryByAdvisorType(String advisorType);

    /**
     * 查询所有启用的顾问
     * @return 顾问列表
     */
    List<AiClientAdvisor> queryAllEnabled();

    /**
     * 删除顾问
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);
}