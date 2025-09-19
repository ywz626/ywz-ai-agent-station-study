package com.ywzai.domain.agent.model.entity;


import lombok.Data;

import java.util.List;

/**
 * @Author: ywz
 * @CreateTime: 2025-09-19
 * @Description: 命令配置类
 * @Version: 1.0
 */
@Data
public class ArmoryCommendEntity {
    private String commendType;
    private List<String> commendList;
}
