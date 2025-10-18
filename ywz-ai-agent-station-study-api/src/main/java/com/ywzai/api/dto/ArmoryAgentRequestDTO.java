package com.ywzai.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArmoryAgentRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * AI智能体ID
     */
    private String agentId;

}