package com.ywzai.infrastructure.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminUser {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID（唯一标识）
     */
    private String userId;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 状态(0:禁用,1:启用,2:锁定)
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}