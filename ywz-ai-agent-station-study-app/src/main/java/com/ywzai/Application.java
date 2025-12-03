package com.ywzai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Configurable
@MapperScan(
    basePackages = "com.ywzai.infrastructure.dao",
    sqlSessionFactoryRef = "sqlSessionFactoryBean" // 必须指向你的 Bean 名
    )
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class);
  }
}
