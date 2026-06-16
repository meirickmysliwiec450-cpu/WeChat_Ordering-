# 微信点餐系统 - 后端服务

基于 Spring Boot 2.7 + MyBatis + MySQL 的微信点餐系统后端，为商家管理后台和微信小程序提供 RESTful API 服务。

---

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.14 | 核心框架 |
| MyBatis | 3.5.11 | ORM 数据持久层 |
| MySQL | 8.0 | 关系型数据库 |
| Druid | 1.2.18 | 数据库连接池 |
| JWT (jjwt) | 0.9.1 | 无状态身份认证 |
| Maven | 3.x | 项目构建与依赖管理 |
| Java | 1.8+ | 开发语言 |

---

## 项目结构

```
src/main/java/com/wechat/ordering/
├── config/          # 配置类（CORS跨域、JWT认证拦截器）
├── controller/      # REST API 控制器（8个模块、30+个接口）
├── service/         # 业务逻辑接口
├── service/impl/    # 业务逻辑实现
├── mapper/          # MyBatis Mapper 接口（12个数据访问接口）
├── entity/          # 数据库实体类（12张业务表）
├── util/            # 工具类（Result统一响应、JwtUtil令牌工具）
└── WxOrderingApplication.java  # Spring Boot 启动类
```

---

## 已完成功能

### 1. 管理员认证模块
- 管理员登录：验证用户名密码，登录成功后返回 JWT Token
- Token 认证拦截：所有 `/admin/**` 接口自动校验 Bearer Token，未登录返回 401
- 管理员信息查询：根据 Token 中的 adminId 获取管理员详细信息

### 2. 仪表盘数据统计
- 总用户数统计
- 总订单数统计
- 总营业额统计（所有已支付订单金额之和）
- 总菜品数量统计
- 今日新增订单数
- 待处理反馈数
- 订单状态分布（待处理 / 已接单 / 已完成）

### 3. 用户管理
- 用户列表分页查询
- 用户详情查询（昵称、头像、性别、手机号、OpenID）
- 用户信息编辑
- 用户状态管理（启用 / 禁用）

### 4. 菜品分类管理
- 分类列表查询（支持排序）
- 分类新增（设置分类名称和排序号）
- 分类信息编辑
- 分类删除

### 5. 菜品管理
- 菜品分页查询（支持按分类筛选 + 关键词搜索）
- 菜品详情查询
- 菜品新增（名称、分类、价格、库存、折扣、图片URL、描述）
- 菜品信息编辑
- 菜品上下架状态切换
- 菜品删除

### 6. 订单管理
- 订单列表分页查询（支持按订单状态筛选：已取消 / 待处理 / 已接单 / 已完成）
- 订单详情查询（含订单基本信息 + 订单明细菜品列表）
- 订单状态流转：接单 → 完成 / 拒单
- 订单完成时自动更新支付状态为已支付

### 7. 轮播图管理
- 轮播图列表查询
- 轮播图详情查询
- 轮播图新增（标题、图片URL、跳转链接、排序、备注）
- 轮播图信息编辑
- 轮播图启用/禁用状态切换
- 轮播图删除

### 8. 用户反馈管理
- 反馈列表分页查询（支持按处理状态筛选：未处理 / 已回复 / 已解决）
- 反馈详情查询
- 管理员回复用户反馈（回复后自动标记为"已回复"）
- 反馈状态更新（标记已解决）

---

## 数据库设计

共 **12 张业务表**，执行 `src/main/resources/db.sql` 初始化：

| 表名 | 说明 | 核心字段 |
|------|------|------|
| admin | 管理员 | 用户名、密码、真实姓名、手机号 |
| user | 微信用户 | OpenID、昵称、头像、性别、手机号 |
| category | 菜品分类 | 分类名称、排序号 |
| dish | 菜品 | 分类ID、名称、价格、库存、折扣、销量、图片、描述 |
| order | 订单 | 订单号、用户ID、总金额、支付状态、订单状态、收货信息 |
| order_detail | 订单明细 | 订单ID、菜品ID、单价、数量、小计 |
| cart | 购物车 | 用户ID、菜品ID、数量、总价 |
| address | 收货地址 | 用户ID、收货人、电话、地址详情、是否默认 |
| banner | 轮播图 | 标题、图片URL、跳转链接、排序、状态 |
| feedback | 用户反馈 | 用户ID、内容、回复内容、处理状态 |
| payment | 支付记录 | 订单ID、支付流水号、支付金额、支付方式 |
| order_comment | 订单评价 | 订单ID、用户ID、评分、内容、图片 |

默认初始化数据：
- 管理员账号：`admin` / `123456`
- 6 个菜品分类（热菜、凉菜、汤品、主食、饮品、小吃）
- 3 个轮播图示例

---

## 安全设计

- **JWT 无状态认证**：登录后签发 Token，有效期 7 天
- **认证拦截器**：`AuthInterceptor` 拦截所有 `/admin/**` 请求，校验 Authorization Bearer Token
- **CORS 跨域配置**：允许前端独立域名访问
- **密码加密存储**：管理员密码使用 MD5 加密后存储
- **统一响应格式**：所有接口返回 `{ code, message, data }` 标准 JSON 结构

---

## 启动方式

```bash
# 1. 确保 MySQL 8.0 已启动，执行 db.sql 初始化数据库
# 2. 确认 application.properties 中数据库连接配置正确
# 3. 编译并启动
mvn spring-boot:run
```

服务启动在 **http://localhost:8080/api**

---

## API 接口总览

| 模块 | 接口路径 | 方法 | 功能说明 | 认证 |
|------|------|------|------|------|
| 认证 | `/api/admin/auth/login` | POST | 管理员登录 | 否 |
| 认证 | `/api/admin/auth/info` | GET | 获取管理员信息 | 是 |
| 仪表盘 | `/api/admin/dashboard/stats` | GET | 获取统计数据 | 是 |
| 用户 | `/api/admin/users` | GET | 用户列表（分页） | 是 |
| 用户 | `/api/admin/users/{id}` | GET | 用户详情 | 是 |
| 用户 | `/api/admin/users/{id}` | PUT | 编辑用户信息 | 是 |
| 用户 | `/api/admin/users/{id}/status` | PUT | 启用/禁用用户 | 是 |
| 分类 | `/api/admin/categories` | GET/POST | 列表查询 / 新增 | 是 |
| 分类 | `/api/admin/categories/{id}` | GET/PUT/DELETE | 详情/编辑/删除 | 是 |
| 菜品 | `/api/admin/dishes` | GET/POST | 分页查询 / 新增 | 是 |
| 菜品 | `/api/admin/dishes/{id}` | GET/PUT/DELETE | 详情/编辑/删除 | 是 |
| 菜品 | `/api/admin/dishes/{id}/status` | PUT | 上架/下架 | 是 |
| 订单 | `/api/admin/orders` | GET | 订单列表（分页+筛选） | 是 |
| 订单 | `/api/admin/orders/{id}` | GET | 订单详情（含明细） | 是 |
| 订单 | `/api/admin/orders/{id}/status` | PUT | 更新订单状态 | 是 |
| 轮播图 | `/api/admin/banners` | GET/POST | 列表查询 / 新增 | 是 |
| 轮播图 | `/api/admin/banners/{id}` | GET/PUT/DELETE | 详情/编辑/删除 | 是 |
| 轮播图 | `/api/admin/banners/{id}/status` | PUT | 启用/禁用 | 是 |
| 反馈 | `/api/admin/feedbacks` | GET | 反馈列表（分页+筛选） | 是 |
| 反馈 | `/api/admin/feedbacks/{id}` | GET | 反馈详情 | 是 |
| 反馈 | `/api/admin/feedbacks/{id}/reply` | POST | 管理员回复 | 是 |
| 反馈 | `/api/admin/feedbacks/{id}/status` | PUT | 更新处理状态 | 是 |
