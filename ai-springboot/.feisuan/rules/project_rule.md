
# 开发规范指南
为保证代码质量、可维护性、安全性与可扩展性，请在开发过程中严格遵循以下规范。

## 一、项目概述与环境

- **项目名称**：ai-sprinboot
- **工作目录**：`D:\xljkai\ideacode\ai-sprinboot`
- **代码作者**：PANJU
- **操作系统**：Windows 11

## 二、技术栈要求

- **主框架**：Spring Boot 4.1.0
- **语言版本**：Java 17
- **构建工具**：Maven
- **核心依赖**：
  - `spring-boot-starter-webmvc` (Web开发)
  - `spring-boot-starter-data-jdbc` (数据库交互，非JPA)
  - `mysql-connector-j` (MySQL驱动)
  - `lombok` (简化代码)

## 三、项目目录结构

项目遵循标准的 Maven 目录结构，具体如下：

```text
ai-sprinboot
└── src
    ├── main
    │   ├── java
    │   │   └── org
    │   │       └── example
    │   │           └── aisprinboot
    │   │               ├── common          // 通用组件与配置
    │   │               ├── controller      // 控制器层
    │   │               ├── DTO             // 数据传输对象
    │   │               │   ├── command     // 命令(入参) DTO
    │   │               │   └── response    // 响应(出参) DTO
    │   │               ├── entity          // 数据库实体
    │   │               ├── exception       // 自定义异常处理
    │   │               ├── mapper          // 数据访问层
    │   │               └── service         // 业务逻辑层
    │   └── resources
    │       ├── static          // 静态资源
    │       └── templates       // 模板文件
    └── test
        └── java
            └── org
                └── example
                    └── aisprinboot
```

## 四、分层架构规范

| 层级        | 职责说明                         | 开发约束与注意事项                                               |
|-------------|----------------------------------|----------------------------------------------------------------|
| **Controller** | 处理 HTTP 请求与响应，定义 API 接口 | 不得直接访问数据库，必须通过 Service 层调用                  |
| **Service**    | 实现业务逻辑、事务管理与数据校验   | 必须通过 Mapper 层访问数据库；返回 DTO 而非 Entity            |
| **Mapper**     | 数据库访问与持久化操作             | 使用 `@Mapper` 注解或 XML 配置进行 SQL 映射；注意防止 SQL 注入 |
| **Entity**     | 映射数据库表结构                   | 不得直接返回给前端（需转换为 DTO）；包名统一为 `entity`         |
| **DTO**        | 数据传输对象                      | 细分为 `command` (入参) 和 `response` (出参) 包管理           |

### 接口与实现分离

- 所有接口实现类需放在接口所在包下的 `impl` 子包中（如 `service` 包下创建 `impl` 包）。

## 五、安全与性能规范

### 输入校验

- 使用 `@Valid` 与 JSR-303 校验注解（如 `@NotBlank`, `@Size` 等）。
  - 注意：Spring Boot 4.x 中校验注解位于 `jakarta.validation.constraints.*`。
- **特别注意**：本项目使用 JDBC Template 或 MyBatis 风格的 `Mapper`，**严禁**手动拼接 SQL 字符串，必须使用参数化查询（如 `?` 占位符或 `#{param}` 语法）以防止 SQL 注入。

### 事务管理

- `@Transactional` 注解仅用于 **Service 层**方法。
- 避免在循环中频繁提交事务，影响性能。

## 六、代码风格规范

### 命名规范

| 类型       | 命名方式             | 示例                  |
|------------|----------------------|-----------------------|
| 类名       | UpperCamelCase       | `UserServiceImpl`     |
| 方法/变量  | lowerCamelCase       | `saveUser()`          |
| 常量       | UPPER_SNAKE_CASE     | `MAX_LOGIN_ATTEMPTS`  |

### 注释规范

- 所有类、方法、字段需添加 **Javadoc** 注释。
- **注释语言**：统一使用**中文**（根据用户 PANJU 的语言环境设定）。
- 作者信息标注为 `@author PANJU`。

### 类型命名规范（阿里巴巴风格）

| 后缀 | 用途说明                     | 示例         | 存放包路径           |
|------|------------------------------|--------------|----------------------|
| DTO  | 数据传输对象                 | `UserDTO`    | `DTO.command` / `DTO.response` |
| Entity| 数据库实体对象               | `UserEntity` | `entity`             |
| VO   | 视图展示对象                 | `UserVO`     | `DTO.response`       |
| Query| 查询参数封装对象             | `UserQuery`  | `DTO.command`        |

### 实体类简化工具

- 使用 Lombok 注解替代手动编写 getter/setter/构造方法：
  - `@Data`
  - `@NoArgsConstructor`
  - `@AllArgsConstructor`

## 七、扩展性与日志规范

### 接口优先原则

- 所有业务逻辑通过接口定义（如 `UserService`），具体实现放在 `impl` 包中（如 `UserServiceImpl`）。

### 日志记录

- 使用 `@Slf4j` 注解代替 `System.out.println`。

## 八、编码原则总结

| 原则       | 说明                                       |
|------------|--------------------------------------------|
| **SOLID**  | 高内聚、低耦合，增强可维护性与可扩展性     |
| **DRY**    | 避免重复代码，提高复用性                   |
| **KISS**   | 保持代码简洁易懂                           |
| **YAGNI**  | 不实现当前不需要的功能                     |
| **OWASP**  | 防范常见安全漏洞，如 SQL 注入、XSS 等      |
