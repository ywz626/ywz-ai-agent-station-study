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
public class AiClientSystemPromptRequestDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 主键ID（更新时使用） */
  private Long id;

  /** 提示词ID */
  private String promptId;

  /** 提示词名称 */
  private String promptName;

  /** 提示词内容 */
  private String promptContent;

  /** 描述 */
  private String description;

  /** 状态(0:禁用,1:启用) */
  private Integer status;
}
