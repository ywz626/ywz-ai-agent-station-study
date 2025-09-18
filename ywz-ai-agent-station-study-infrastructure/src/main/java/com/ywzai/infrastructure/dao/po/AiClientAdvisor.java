package com.ywzai.infrastructure.dao.po;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-18
 * @Description: Ai的顾问PO
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiClientAdvisor {
    private Long id;
    private String advisorId;
    private String advisorName;
    private String advisorType;
    private Integer orderNum;
    private String extParam;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
