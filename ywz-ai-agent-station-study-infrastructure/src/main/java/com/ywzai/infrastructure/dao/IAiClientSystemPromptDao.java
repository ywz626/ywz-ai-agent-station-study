package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiClientSystemPrompt;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: AI客户端系统提示词DAO接口
 * @Version: 1.0
 */
@Mapper
public interface IAiClientSystemPromptDao {

    /**
     * 插入系统提示词
     * @param prompt 提示词对象
     * @return 影响行数
     */
    int insert(AiClientSystemPrompt prompt);

    /**
     * 更新系统提示词
     * @param prompt 提示词对象
     * @return 影响行数
     */
    int update(AiClientSystemPrompt prompt);

    /**
     * 根据提示词ID查询
     * @param promptId 提示词ID
     * @return 提示词对象
     */
    AiClientSystemPrompt queryByPromptId(String promptId);

    /**
     * 根据提示词名称查询
     * @param promptName 提示词名称
     * @return 提示词对象
     */
    List<AiClientSystemPrompt> queryByPromptName(String promptName);

    /**
     * 查询所有启用的系统提示词
     * @return 提示词列表
     */
    List<AiClientSystemPrompt> queryAllEnabled();

    /**
     * 删除系统提示词
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    int updateById(AiClientSystemPrompt aiClientSystemPrompt);

    int updateByPromptId(AiClientSystemPrompt aiClientSystemPrompt);

    int deleteByPromptId(String promptId);

    AiClientSystemPrompt queryById(Long id);

    List<AiClientSystemPrompt> queryAll();

    List<AiClientSystemPrompt> queryEnabledPrompts();
}