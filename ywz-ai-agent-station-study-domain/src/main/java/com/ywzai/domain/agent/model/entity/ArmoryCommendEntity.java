package com.ywzai.domain.agent.model.entity;


import com.ywzai.domain.agent.model.valobj.AiAgentEnumVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: 命令配置类
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArmoryCommendEntity {
    private String commendType;
    private List<String> commendList;



    public String getLoadDataStrategy(){
        return AiAgentEnumVO.getByCode(commendType).getLoadDataStrategy();
    }
}
