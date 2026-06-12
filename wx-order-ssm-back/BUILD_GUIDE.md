# 项目构建指南

## 项目概述

这是一个使用 Spring Boot + MyBatis + MySQL 的微信点餐系统后端项目。

## 项目结构说明

```
wx-order-ssm-back/
├── pom.xml                           # Maven 项目配置文件
├── .gitignore                        # Git 忽略文件配置
├── README.md                         # 项目说明文档
├── src/
│   ├── main/
│   │   ├── java/com/wechat/ordering/ # Java 源代码
│   │   │   ├── controller/           # 控制层（API 接口）
│   │   │   ├── service/              # 业务逻辑层
│   │   │   ├── mapper/               # 数据访问层（MyBatis）
│   │   │   ├── entity/               # 数据模型
│   │   │   ├── config/               # 配置类
│   │   │   ├── util/                 # 工具类
│   │   │   └── WxOrderingApplication.java # 主启动类
│   │   └── resources/                # 资源文件
│   │       ├── application.properties # 应用配置
│   │       ├── db.sql                # 数据库初始化脚本
│   │       └── mybatis/
│   │           └── mapper/           # MyBatis XML 映射文件
│   └── test/
│       ├── java/                     # 单元测试代码
│       └── resources/                # 测试资源
└── target/                           # 编译输出（Maven 生成，不提交）
```

## 环境要求

- Java 1.8+
- Maven 3.6+
- MySQL 5.7+ 或 MySQL 8.0+
- Node.js 14+（前端开发）

## 快速开始

### 1. 克隆项目

\`\`\`bash
git clone <repository-url>
cd wx-order-ssm-back
\`\`\`

### 2. 数据库初始化

- 打开 MySQL 客户端
- 执行 \`src/main/resources/db.sql\` 文件中的 SQL 语句
- 或者运行：\`mysql -u root -p < src/main/resources/db.sql\`

### 3. 修改数据库连接

编辑 \`src/main/resources/application.properties\`：
\`\`\`properties
spring.datasource.username=你的MySQL用户名
spring.datasource.password=你的MySQL密码
\`\`\`

### 4. 构建项目

\`\`\`bash
mvn clean install
\`\`\`

### 5. 运行项目

\`\`\`bash
mvn spring-boot:run
\`\`\`

或者：
\`\`\`bash
java -jar target/wx-order-ssm-back-1.0.0.war
\`\`\`

项目启动后，访问：\`http://localhost:8080/api\`

## 项目依赖说明

- **Spring Boot 2.7.14** - 框架基础
- **MyBatis 3.5.11** - ORM 框架
- **MySQL Connector 8.0.33** - 数据库驱动
- **Druid 1.2.18** - 数据库连接池
- **Lombok** - 简化 Java 代码
- **JUnit** - 单元测试框架

## 常用命令

### Maven 常用命令

\`\`\`bash
mvn clean # 清理构建文件
mvn compile # 编译源代码
mvn test # 运行测试
mvn package # 打包项目
mvn install # 安装到本地仓库
mvn deploy # 部署到远程仓库
\`\`\`

## API 接口说明

所有 API 请求路径以 \`/api\` 开头。

### 响应格式

\`\`\`json
{
"code": 200,
"message": "成功",
"data": {}
}
\`\`\`

- **code**: 状态码（200 成功，其他值表示错误）
- **message**: 提示消息
- **data**: 返回的数据（可选）

## 注意事项

1. 确保 MySQL 服务已启动
2. 确保端口 8080 未被占用
3. 数据库编码应设置为 UTF-8 MB4
4. 建议使用 IDE（如 IntelliJ IDEA）进行开发

## 常见问题

### Q: 运行时出现数据库连接错误？

A: 检查 \`application.properties\` 中的数据库连接信息是否正确，确保 MySQL 服务已启动。

### Q: Maven 下载依赖很慢？

A: 可以配置阿里云 Maven 镜像加速。

### Q: 如何生成 API 文档？

A: 可以集成 Swagger 2 或 Springfox 实现自动 API 文档生成。

## 开发者

- 项目名称: 微信点餐系统
- 开发框架: Spring Boot + MyBatis
- 创建日期: 2024年

## 许可证

[MIT License](LICENSE)
