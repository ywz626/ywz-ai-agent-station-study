# 数据访问与持久化

## 整合关系型数据库 (如 MySQL)
在 `pom.xml` 中添加 MySQL 和 JPA 依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

配置 `application.properties` 文件：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/mydb?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

## 使用 NoSQL 数据库 (如 Redis, MongoDB)
### 集成 Redis
在 `pom.xml` 中添加 Redis 依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 示例代码：连接 Redis 并执行基本操作
```java
package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void setKey(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value);
    }

    public String getKey(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }
}
```

## 事务管理
Spring Boot 提供了 `@Transactional` 注解来简化事务管理。

### 示例代码：使用事务管理
```java
package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void addUser(User user) {
        userRepository.save(user);
    }
}
```

通过这些示例，我们可以看到如何在 Spring Boot 中进行数据访问和持久化。