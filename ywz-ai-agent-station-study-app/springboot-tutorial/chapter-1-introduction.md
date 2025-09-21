# Spring Boot 入门

## 介绍
Spring Boot 是一个基于 Spring 框架的快速开发工具，它简化了新 Spring 应用的初始搭建以及开发过程。

## 快速搭建第一个 Spring Boot 项目
### 使用 Spring Initializr 创建项目
访问 [Spring Initializr](https://start.spring.io/) 并选择项目配置，下载项目压缩包。

### 手动配置 Maven 依赖
在 `pom.xml` 中添加以下依赖：
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>
```

## 示例代码：Hello World 应用程序
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

```java
package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello, Spring Boot!";
    }
}
```

运行主程序后，打开浏览器访问 `http://localhost:8080/hello` 查看结果。