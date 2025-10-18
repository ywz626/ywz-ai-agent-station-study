package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiClientToolMcp;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: AI客户端MCP工具DAO接口
 * @Version: 1.0
 */
@Mapper
public interface IAiClientToolMcpDao {

    /**
     * 插入MCP工具配置
     * @param toolMcp MCP工具对象
     * @return 影响行数
     */
    int insert(AiClientToolMcp toolMcp);

    /**
     * 更新MCP工具配置
     * @param toolMcp MCP工具对象
     * @return 影响行数
     */
    int update(AiClientToolMcp toolMcp);

    /**
     * 根据MCP ID查询
     * @param mcpId MCP ID
     * @return MCP工具对象
     */
    AiClientToolMcp queryByMcpId(String mcpId);

    /**
     * 根据传输类型查询
     * @param transportType 传输类型
     * @return MCP工具列表
     */
    List<AiClientToolMcp> queryByTransportType(String transportType);

    /**
     * 查询所有启用的MCP工具
     * @return MCP工具列表
     */
    List<AiClientToolMcp> queryAllEnabled();

    /**
     * 删除MCP工具
     * @param id 主键ID
     * @return 影响行数
     */
    int deleteById(Long id);

    int updateById(AiClientToolMcp aiClientToolMcp);

    int updateByMcpId(AiClientToolMcp aiClientToolMcp);

    int deleteByMcpId(String mcpId);

    AiClientToolMcp queryById(Long id);

    List<AiClientToolMcp> queryAll();

    List<AiClientToolMcp> queryByStatus(Integer status);

    List<AiClientToolMcp> queryEnabledMcps();
}