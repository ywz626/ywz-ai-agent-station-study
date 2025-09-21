# 核心功能与自动配置

## 依赖管理
Spring Boot 提供了 `spring-boot-starter` 依赖，可以方便地引入一组相关的依赖。

## 自动配置原理
Spring Boot 的自动配置基于条件注解（如 `@ConditionalOnClass` 和 `@ConditionalOnMissingBean`），根据类路径中的类和已定义的 Bean 来决定是否启用某些配置。

## 注解驱动开发
使用 `@SpringBootApplication` 注解来简化配置，它包含了 `@Configuration`, `@EnableAutoConfiguration` 和 `@ComponentScan`。

## 示例代码：使用注解简化配置
```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

通过 `@SpringBootApplication` 注解，我们可以快速启动一个 Spring Boot 应用。