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
public class AdminUserLoginRequestDTO implements Serializable {

  @Serial private static final long serialVersionUID = 1L;

  /** 用户名（登录账号） */
  private String username;

  /** 密码 */
  private String password;
}
