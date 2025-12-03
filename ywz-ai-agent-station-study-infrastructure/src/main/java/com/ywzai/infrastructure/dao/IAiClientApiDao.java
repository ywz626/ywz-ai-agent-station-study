package com.ywzai.infrastructure.dao;

import com.ywzai.infrastructure.dao.po.AiClientApi;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

/**
 * @Author: ywz @CreateTime: 2025-09-18 @Description: AI客户端API配置DAO接口 @Version: 1.0
 */
@Mapper
public interface IAiClientApiDao {

  /**
   * 插入API配置
   *
   * @param apiConfig API配置对象
   * @return 影响行数
   */
  int insert(AiClientApi apiConfig);

  /**
   * 更新API配置
   *
   * @param apiConfig API配置对象
   * @return 影响行数
   */
  int update(AiClientApi apiConfig);

  /**
   * 根据API ID查询
   *
   * @param apiId API ID
   * @return API配置对象
   */
  AiClientApi queryByApiId(String apiId);

  /**
   * 根据ID查询
   *
   * @param id 主键ID
   * @return API配置对象
   */
  AiClientApi queryById(Long id);

  /**
   * 查询所有启用的API配置
   *
   * @return API配置列表
   */
  List<AiClientApi> queryAllEnabled();

  /**
   * 删除API配置
   *
   * @param id 主键ID
   * @return 影响行数
   */
  int deleteById(Long id);
}
