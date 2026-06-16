# 微信点餐系统 - 小程序端 API 接口文档

> **Base URL**：`http://10.120.80.146:8080/api`（演示前确认 IP，可能变化）

---

## 认证说明

| 接口 | 是否需要 Token | 说明 |
|------|:---:|------|
| `/wx/user/login` | ❌ | 微信登录，返回 Token |
| 其他所有 `/wx/**` | ✅ | Header 携带 `Authorization: Bearer <token>` |

**统一响应格式**：`{ "code": 200, "message": "成功", "data": {...} }`

---

## 一、用户模块 `/wx/user`

### 1. 微信登录 / 自动注册
```
POST /wx/user/login
```
**请求体**：
```json
{
  "openId": "微信授权返回的openId",
  "nickName": "微信昵称",
  "avatar": "头像URL",
  "gender": 1
}
```
**返回**：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbG...",
    "user": { "id": 1, "openId": "xxx", "nickName": "用户昵称", "avatar": "...", "gender": 1, "phone": null, "status": 1 }
  }
}
```
> 新用户自动注册，老用户自动更新昵称和头像。

### 2. 获取个人信息
```
GET /wx/user/info
Header: Authorization: Bearer <token>
```

### 3. 更新个人信息
```
PUT /wx/user/info
Header: Authorization: Bearer <token>
```
**请求体**：`{ "nickName": "新昵称", "avatar": "...", "gender": 1, "phone": "13800138000" }`（全部可选）

---

## 二、轮播图模块 `/wx/banners`

### 获取首页轮播图列表
```
GET /wx/banners
```
**说明**：只返回 `status=1`（启用）的轮播图，按 `sort` 排序。

**返回示例**：
```json
{
  "code": 200,
  "data": [
    { "id": 1, "title": "新品推荐", "imageUrl": "/images/热菜/经典红烧肉.png", "linkUrl": "", "sort": 1, "status": 1 }
  ]
}
```
> 图片完整URL = Base URL + imageUrl，例如 `http://10.120.80.146:8080/api/images/热菜/经典红烧肉.png`

---

## 三、分类模块 `/wx/categories`

### 获取所有分类（含菜品列表）
```
GET /wx/categories
```
**返回示例**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1, "categoryName": "热菜", "sort": 1,
      "dishes": [
        { "id": 1, "dishName": "经典红烧肉", "price": 35.00, "image": "/images/热菜/经典红烧肉.png", "sales": 120, "stock": 50, "status": 1 }
      ]
    },
    { "id": 2, "categoryName": "冷菜", "sort": 2, "dishes": [...] }
  ]
}
```
> 只返回已上架（status=1）的菜品，按分类 sort 排序。

---

## 四、菜品模块 `/wx/dishes`

### 1. 按分类获取菜品
```
GET /wx/dishes?categoryId=1
```
> 只返回已上架的菜品。

### 2. 搜索菜品
```
GET /wx/dishes/search?keyword=红烧
```
> 支持菜名模糊搜索。

### 3. 菜品详情
```
GET /wx/dishes/{id}
```

---

## 五、购物车模块 `/wx/cart`

> 所有接口需要 Token。

### 1. 查看购物车
```
GET /wx/cart
```
**返回示例**：
```json
{
  "code": 200,
  "data": [
    { "id": 1, "dishId": 1, "dishName": "经典红烧肉", "image": "/images/热菜/经典红烧肉.png", "price": 35.00, "quantity": 2, "totalPrice": 70.00, "stock": 50 }
  ]
}
```

### 2. 添加菜品
```
POST /wx/cart
```
**请求体**：`{ "dishId": 1, "quantity": 2 }`
> 购物车已有该菜品则累加数量。

### 3. 修改数量
```
PUT /wx/cart/{id}
```
**请求体**：`{ "quantity": 3 }`
> 数量 ≤ 0 则删除该项。

### 4. 删除某项
```
DELETE /wx/cart/{id}
```

### 5. 清空购物车
```
DELETE /wx/cart/clear
```

---

## 六、收货地址模块 `/wx/address`

> 所有接口需要 Token。

### 1. 地址列表
```
GET /wx/address
```

### 2. 新增地址
```
POST /wx/address
```
**请求体**：
```json
{ "receiver": "张三", "phone": "13800138000", "addressDetail": "XX大学XX宿舍", "isDefault": 1 }
```
> isDefault=1 则自动取消其他默认地址。

### 3. 编辑地址
```
PUT /wx/address/{id}
```

### 4. 删除地址
```
DELETE /wx/address/{id}
```

### 5. 设为默认
```
PUT /wx/address/{id}/default
```

---

## 七、订单模块 `/wx/orders`

> 所有接口需要 Token。

### 1. 提交订单（从购物车生成）
```
POST /wx/orders
```
**请求体**：
```json
{ "addressId": 1, "remark": "少放辣" }
```
**返回**：
```json
{ "code": 200, "data": { "orderId": 10, "orderNo": "WX202606161430001234", "totalAmount": 85.00 } }
```
> 自动从购物车生成订单和明细、扣库存、增销量、清空购物车。订单状态初始为1（待处理）。

### 2. 我的订单列表
```
GET /wx/orders?page=1&pageSize=10&status=1
```
> status 可选：0已取消 / 1待处理 / 2已接单 / 3已完成。不传返回全部，按时间倒序。

### 3. 订单详情
```
GET /wx/orders/{id}
```
**返回**：`{ "order": {...}, "details": [{ "dishName": "...", "price": 35, "quantity": 2, "amount": 70 }, ...] }`

### 4. 取消订单
```
PUT /wx/orders/{id}/cancel
```
> 仅待处理（status=1）的订单可取消。

---

## 八、支付模块 `/wx/payments`

> 需要 Token。

### 发起支付（模拟）
```
POST /wx/payments
```
**请求体**：
```json
{ "orderId": 10, "payMethod": "wechat" }
```
> payMethod: wechat 或 alipay。
>
> **返回**：`{ "payNo": "PAY202606161430001234", "payAmount": 85.00 }`
>
> 自动更新订单支付状态为已支付。

---

## 九、反馈模块 `/wx/feedbacks`

> 需要 Token。

### 1. 提交反馈
```
POST /wx/feedbacks
```
**请求体**：`{ "content": "建议增加新菜品" }`

### 2. 我的反馈列表
```
GET /wx/feedbacks
```
**返回**：含 `replyContent`（管理员回复）和 `status`（0未处理/1已回复/2已解决）。

---

## 十、评价模块 `/wx/comments`

> 需要 Token。

### 1. 评价已完成订单
```
POST /wx/comments
```
**请求体**：
```json
{ "orderId": 10, "score": 5, "content": "味道很好！", "photo": "图片URL（可选）" }
```
> 仅已完成（status=3）的订单可评价，每个订单只能评价一次。

### 2. 查看订单评价
```
GET /wx/comments/order/{orderId}
```

---

## 十一、AI 个性化推荐 `/wx/recommend`  ⭐ 新功能

> 需要 Token。

### 今日为你推荐
```
GET /wx/recommend
```
**说明**：根据用户历史订单 + 当前时段（早/午/晚餐），AI 智能推荐 3 道菜品。

**返回示例**：
```json
{
  "code": 200,
  "data": {
    "period": "午餐",
    "dishes": [
      {
        "id": 1, "dishName": "经典红烧肉", "image": "/images/热菜/经典红烧肉.png",
        "price": 35.00, "sales": 120, "categoryName": "热菜",
        "reason": "您之前多次点红烧肉，搭配一道清爽的汤品更佳"
      },
      { "id": 3, "dishName": "蒜泥白肉", "reason": "适合午餐食用的经典冷菜" },
      { "id": 10, "dishName": "冰红茶", "reason": "夏季午餐的清爽饮品搭配" }
    ]
  }
}
```
> 如果 AI 调用失败，自动切换为兜底推荐（基于销量和用户历史的简单推荐），返回中会有 `"fallback": true` 标记。

---

## 接口汇总（28个）

| 模块 | 路径 | 接口数 | 需Token |
|------|------|:---:|:---:|
| 用户 | `/wx/user` | 3 | login 无，其余有 |
| 轮播图 | `/wx/banners` | 1 | ❌ |
| 分类 | `/wx/categories` | 1 | ❌ |
| 菜品 | `/wx/dishes` | 3 | ❌ |
| 购物车 | `/wx/cart` | 5 | ✅ |
| 地址 | `/wx/address` | 5 | ✅ |
| 订单 | `/wx/orders` | 4 | ✅ |
| 支付 | `/wx/payments` | 1 | ✅ |
| 反馈 | `/wx/feedbacks` | 2 | ✅ |
| 评价 | `/wx/comments` | 2 | ✅ |
| AI推荐 | `/wx/recommend` | 1 | ✅ |
| **合计** | | **28** | |

---

## 小程序配置建议

```js
// config.js 放在小程序根目录，演示前改 IP 即可
const CONFIG = {
  baseURL: 'http://10.120.80.146:8080/api',  // 改这一行
  imageBase: 'http://10.120.80.146:8080/api', // 图片拼接用
  timeout: 10000,
  version: '1.0.0'
}
module.exports = CONFIG
```

## 图片使用方式

图片在数据库里存的是相对路径（如 `/images/热菜/经典红烧肉.png`），前端拼上 baseURL：
```js
<image src="{{imageBase + dish.image}}" />
// 结果：http://10.120.80.146:8080/api/images/热菜/经典红烧肉.png
```
