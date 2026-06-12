# 微信点餐系统 - Java后端项目快速开始

## 📁 文件结构

```
wx-order-ssm-back/
├── src/main/resources/
│   ├── db.sql                    ✅ 数据库建表SQL
│   ├── application.properties    ← 数据库连接配置
│   └── mybatis/
│       └── config/
│           └── mybatis-config.xml
├── src/main/java/
│   └── com/wechat/ordering/
│       ├── entity/               ← 实体类
│       ├── dao/                  ← 数据访问层
│       ├── service/              ← 业务逻辑层
│       ├── controller/           ← 控制层
│       └── config/               ← 配置类
└── pom.xml
```

## 🚀 快速开始步骤

### 1️⃣ 数据库设置

```bash
# 连接MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE wechat_ordering DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 执行SQL建表脚本
USE wechat_ordering;
SOURCE wx-order-ssm-back/src/main/resources/db.sql;
```

### 2️⃣ 配置数据库连接

编辑 `application.properties`：

```properties
# 数据库连接信息
spring.datasource.url=jdbc:mysql://localhost:3306/wechat_ordering?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis配置
mybatis.mapper-locations=classpath:mybatis/mapper/*.xml
mybatis.config-location=classpath:mybatis/config/mybatis-config.xml
mybatis.type-aliases-package=com.wechat.ordering.entity
```

### 3️⃣ Maven依赖

在 `pom.xml` 中添加（如果还没有）：

```xml
<!-- MySQL驱动 -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>

<!-- MyBatis -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.3.1</version>
</dependency>

<!-- 数据库连接池 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.2.18</version>
</dependency>

<!-- Lombok（简化Entity代码） -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <version>1.18.30</version>
    <scope>provided</scope>
</dependency>
```

## 📊 数据库表说明

| 表名            | 说明       | 备注           |
| --------------- | ---------- | -------------- |
| t_admin         | 管理员表   | 后台管理员账户 |
| t_user          | 用户表     | 微信小程序用户 |
| t_category      | 菜品分类表 | 菜品分类       |
| t_dish          | 菜品表     | 所有菜品信息   |
| t_address       | 收货地址表 | 用户收货地址   |
| t_cart          | 购物车表   | 用户购物车     |
| t_order         | 订单表     | 订单主表       |
| t_order_detail  | 订单详情表 | 订单中的菜品   |
| t_payment       | 支付记录表 | 支付流水记录   |
| t_order_comment | 订单评价表 | 用户订单评价   |
| t_feedback      | 用户反馈表 | 用户反馈意见   |
| t_banner        | 轮播图表   | 首页轮播图     |

## 🔑 关键字段说明

### 命名规则

- **表名**：下划线连接（如 `t_user`、`t_order`）
- **列名**：下划线连接（如 `user_id`、`create_time`）
- **主键**：统一使用 `id`（BIGINT）
- **时间戳**：统一使用 `create_time` DATETIME

### 订单状态 (OrderStatus)

- 0: 待支付
- 1: 已支付
- 2: 配送中
- 3: 已完成
- 4: 已取消

### 支付状态 (PayStatus)

- 0: 未支付
- 1: 已支付
- 2: 支付失败

## ✅ 初始化数据

数据库创建时已自动插入：

- **默认管理员**：username=`admin`, password=`123456`
- **菜品分类**：热菜、冷菜、汤类、米饭、面食、饮品
- **轮播图示例**：3张轮播图

## 📝 常用SQL查询示例

```sql
-- 查询所有菜品
SELECT * FROM t_dish WHERE status = 1;

-- 查询用户订单
SELECT * FROM t_order WHERE user_id = ? ORDER BY create_time DESC;

-- 查询订单详情
SELECT od.*, d.image, d.price as current_price
FROM t_order_detail od
JOIN t_dish d ON od.dish_id = d.id
WHERE od.order_id = ?;

-- 统计销量
SELECT dish_name, SUM(quantity) as total_sales
FROM t_order_detail
GROUP BY dish_id
ORDER BY total_sales DESC;
```

## 🔗 数据库关系图

```
t_user (用户)
  ├── 1:N → t_cart (购物车)
  ├── 1:N → t_address (地址)
  ├── 1:N → t_order (订单)
  ├── 1:N → t_feedback (反馈)
  └── 1:N → t_order_comment (评价)

t_category (分类)
  └── 1:N → t_dish (菜品)

t_dish (菜品)
  ├── 1:N → t_cart (购物车项)
  └── 1:N → t_order_detail (订单详情)

t_order (订单)
  ├── 1:1 → t_payment (支付)
  ├── 1:N → t_order_detail (详情)
  └── 1:N → t_order_comment (评价)
```

## 🛠️ 下一步建议

1. 根据 `db.sql` 中的表结构创建对应的Entity类
2. 创建 MyBatis Mapper 接口和 XML 文件
3. 实现 Service 业务逻辑层
4. 开发 Controller REST API
5. 集成微信支付和登录接口
