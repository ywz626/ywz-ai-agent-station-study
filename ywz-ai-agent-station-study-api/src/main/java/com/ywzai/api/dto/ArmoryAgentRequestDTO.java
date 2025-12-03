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
public class ArmoryAgentRequestDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** AI智能体ID */
  private String agentId;
}
