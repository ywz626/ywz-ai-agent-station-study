package com.ywzai.api;

import com.ywzai.api.dto.DataStatisticsResponseDTO;
import com.ywzai.api.response.Response;

public interface IAiAgentDataStatisticsAdminService {

  /**
   * 获取系统数据统计
   *
   * @return 统计数据响应
   */
  Response<DataStatisticsResponseDTO> getDataStatistics();
}
