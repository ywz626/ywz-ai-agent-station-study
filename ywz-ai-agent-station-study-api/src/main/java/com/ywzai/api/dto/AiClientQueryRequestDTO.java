package com.ywzai.api.dto;

import java.io.Serial;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientQueryRequestDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 客户端ID */
  private String clientId;

  /** 客户端名称（模糊查询） */
  private String clientName;

  /** 状态(0:禁用,1:启用) */
  private Integer status;

  /** 页码（从1开始） */
  private Integer pageNum;

  /** 每页大小 */
  private Integer pageSize;
}
