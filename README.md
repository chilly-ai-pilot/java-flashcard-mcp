# java-flashcard-mcp

一个基于 [Spring AI](https://docs.spring.io/spring-ai/reference/) 和 [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) 构建的抽认卡（Flashcard）MCP Server，通过 stdio 传输与 Claude Desktop、VS Code 等 MCP 客户端集成，让 AI 助手能够直接创建和查询抽认卡数据。

## 特性

- 基于 Spring AI MCP Server Starter，使用 stdio 传输协议
- 内置 H2 内存数据库，通过 Spring Data JPA 持久化抽认卡数据
- 暴露 2 个 MCP 工具：创建抽认卡、查询全部抽认卡
- 日志完全输出到 stderr，避免污染 stdio 协议流

## 技术栈

| 组件 | 版本 |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.2 |
| Spring AI | 1.0.0-M7 |
| 数据库 | MySQL 8.x |
| 构建工具 | Maven |

## 项目结构

```
src/main/java/com/chilly/flashcardmcp/
├── FlashcardMcpApplication.java       # 启动类
├── config/
│   └── FlashcardToolConfiguration.java # 注册 ToolCallbackProvider
├── model/
│   └── Flashcard.java                  # JPA 实体
├── repository/
│   └── FlashcardRepository.java        # JPA Repository
└── tool/
    └── FlashcardTool.java              # MCP 工具定义（@Tool 方法）
src/main/resources/
├── application.yml
└── logback-spring.xml
```

## 可用工具（MCP Tools）

| 工具名 | 说明 | 参数 |
|---|---|---|
| `createFlashcard` | 创建一张新的抽认卡并持久化到数据库 | `title`（标题）、`content`（内容） |
| `listCards` | 查询并返回所有已保存的抽认卡 | 无 |

> **注意**：`pom.xml` 中需要包含 MySQL JDBC 驱动依赖，例如：
> ```xml
> <dependency>
>     <groupId>com.mysql</groupId>
>     <artifactId>mysql-connector-j</artifactId>
>     <scope>runtime</scope>
> </dependency>
> ```

## 快速开始

### 1. 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.x（本地或远程均可，需提前创建好数据库）

### 2. 准备数据库

```sql
CREATE DATABASE flashcarddb CHARACTER SET utf8mb4;
```

表结构由 Hibernate 根据 `spring.jpa.hibernate.ddl-auto: update` 自动创建/更新，无需手动建表。

### 3. 构建

```bash
mvn clean package -DskipTests
```

构建完成后会在 `target/` 目录下生成可执行 jar：

```
target/java-flashcard-mcp-0.0.1-SNAPSHOT.jar
```

### 4. 独立运行（可选，用于本地调试）

```bash
java -jar target/java-flashcard-mcp-0.0.1-SNAPSHOT.jar
```

Server 启动后会以 stdio 模式等待 MCP 客户端发起 JSON-RPC 请求。

## 接入 Claude Desktop

编辑 Claude Desktop 的配置文件：

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`

添加以下配置（**`command` 必须使用 JDK 可执行文件的绝对路径**，不要依赖 PATH，避免 GUI 应用因环境变量差异找不到 `java`）：

```json
{
  "mcpServers": {
    "flashcard-mcp": {
      "command": "/path/to/your/jdk-21/bin/java",
      "args": [
        "-jar",
        "/absolute/path/to/java-flashcard-mcp/target/java-flashcard-mcp-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

> 可通过 `/usr/libexec/java_home -v 21`（macOS）查找 JDK 21 的安装路径。

保存后**完全退出并重新打开** Claude Desktop（不是关闭窗口）以加载新配置。

## 接入 VS Code

在 VS Code 的 MCP 配置中加入相同的 server 定义，并在 Copilot Chat 中：

1. 切换到 **Agent** 模式（而非 Ask / Edit）
2. 点击输入框旁的 🔧 图标，确认已启用 `flashcard-mcp` 的工具
3. 首次调用时确认授权弹窗

## 配置说明

关键配置位于 `src/main/resources/application.yml`：

```yaml
spring:
  ai:
    mcp:
      server:
        enabled: true
        stdio: true        # 使用 stdio 传输
        type: SYNC          # 同步工具调用
  main:
    banner-mode: "off"      # 关闭启动横幅，避免污染 stdout
  datasource:
    url: jdbc:mysql://localhost:3306/flashcarddb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: root
    password: root
```

> 生产环境不建议在配置文件里明文写数据库密码，可通过环境变量注入，例如 `password: ${DB_PASSWORD}`。

日志通过 `logback-spring.xml` 强制输出到 **stderr**，确保 stdout 只承载纯净的 JSON-RPC 消息流：

```xml
<appender name="STDERR" class="ch.qos.logback.core.ConsoleAppender">
    <target>System.err</target>
    ...
</appender>
```

> ⚠️ 这一点对 stdio 型 MCP Server 至关重要：任何意外打到 stdout 的日志或框架横幅，都会破坏 JSON-RPC 消息解析，导致客户端出现"工具能发现但调用失败"的问题。

## 数据存储说明

使用 MySQL 持久化存储，数据在进程重启后依然保留。表结构（`flashcards`）由 Hibernate 通过 `ddl-auto: update` 自动维护，字段变更会自动同步，但生产环境建议改用 Flyway / Liquibase 等迁移工具做更可控的 schema 管理。

## 故障排查

| 现象 | 可能原因 |
|---|---|
| 工具能被发现（`tools/list` 正常）但调用失败 | stdout 被日志/横幅污染，检查 `banner-mode` 和 logback 配置是否只输出到 stderr |
| Claude Desktop 报 `spawn java ENOENT` | `command` 用了裸 `"java"` 而非绝对路径，GUI 应用的 PATH 环境不包含终端里的 Java 路径 |
| 客户端认为工具存在，但模型从不主动调用 | 检查客户端是否处于 Agent 模式、工具是否被手动启用；也可尝试在对话中显式指名工具 |
| 启动报 `Communications link failure` 或连接超时 | MySQL 未启动，或 `datasource.url` 中的主机/端口/数据库名不对，先用 `mysql -u root -p` 手动验证能否连上 |
| 启动报 `Unknown database 'flashcarddb'` | 还没执行 `CREATE DATABASE flashcarddb`，参考上文"准备数据库"步骤 |

## License

MIT