# Web 开发实战

## 构建 RESTful API
Spring Boot 提供了 `@RestController` 和 `@RequestMapping` 注解来快速构建 RESTful 服务。

## 视图解析与模板引擎
### 集成 Thymeleaf
在 `pom.xml` 中添加 Thymeleaf 依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

### 示例代码：使用 Thymeleaf 渲染视图
```java
package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Hello, Thymeleaf!");
        return "home"; // 返回的视图名称
    }
}
```

创建 `src/main/resources/templates/home.html` 文件，内容如下：
```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Home Page</title>
</head>
<body>
    <h1 th:text="${message}"/>
</body>
</html>
```

## 处理请求与响应
### 处理 HTTP 请求参数
```java
@GetMapping("/greet")
public String greet(@RequestParam("name") String name, Model model) {
    model.addAttribute("greeting", "Hello, " + name);
    return "greet";
}
```

### 返回不同格式的响应数据
```java
@GetMapping("/api/greet")
@ResponseBody
public Map<String, String> apiGreet(@RequestParam("name") String name) {
    Map<String, String> response = new HashMap<>();
    response.put("greeting", "Hello, " + name);
    return response;
}
```

通过这些示例，我们可以看到如何使用 Spring Boot 快速开发 Web 应用。