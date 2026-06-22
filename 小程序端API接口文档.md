# 微信点餐系统 - 小程序端 API 接口文档

> **Base URL**：`http://192.168.214.146:8080/api`
>
> 演示前查 IP：`ipconfig` → IPv4 地址，改 Base URL 即可

---

## 一、快速上手

```js
// config.js
export default {
  baseURL: 'http://192.168.214.146:8080/api',
  imageBase: 'http://192.168.214.146:8080/api'  // 图片都拼这个前缀
}
```

```js
// utils/request.js
import CONFIG from '../config'

function request(opts) {
  const token = wx.getStorageSync('token')
  return new Promise((resolve, reject) => {
    wx.request({
      url: CONFIG.baseURL + opts.url,
      method: opts.method || 'GET',
      data: opts.data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': 'Bearer ' + token } : {})
      },
      success(res) {
        if (res.data.code === 200) resolve(res.data.data)
        else if (res.data.code === 401) { wx.removeStorageSync('token'); wx.reLaunch({ url: '/pages/login/login' }) }
        else { wx.showToast({ title: res.data.message, icon: 'none' }); reject(res.data) }
      },
      fail: reject
    })
  })
}
```

---

## 二、认证规则

| 接口 | 需要 Token | 说明 |
|------|:---:|------|
| `/wx/user/login` | ❌ | 登录获取 Token |
| `/wx/banners` `/wx/categories` `/wx/dishes` | ❌ | 公开接口 |
| 其他所有 | ✅ | Header：`Authorization: Bearer <token>` |

- Token 通过 `POST /wx/user/login` 获取
- 统一返回格式：`{ "code": 200, "message": "成功", "data": {...} }`
- code=401 表示 Token 过期，重新登录

---

## 三、接口详解

### 用户 `/wx/user`

#### 登录/注册
```
POST /wx/user/login      不需要 Token
```
| 参数 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| openId | String | 是 | 微信授权 openId |
| nickName | String | 否 | 微信昵称 |
| avatar | String | 否 | 头像 URL |
| gender | Integer | 否 | 0未知 1男 2女 |

请求：`{ "openId": "xxx", "nickName": "小明", "avatar": "https://...", "gender": 1 }`

返回：`{ "token": "eyJhbG...", "user": { "id": 1, "openId": "xxx", "nickName": "小明", "avatar": "...", "phone": null } }`

> 同一 openId 首次调用自动注册，后续调用更新信息并返回 Token

#### 获取个人信息
```
GET /wx/user/info        需要 Token
```
返回：`{ "id": 1, "openId": "...", "nickName": "...", "avatar": "...", "phone": "138...", "gender": 1 }`

#### 更新个人信息
```
PUT /wx/user/info        需要 Token
```
请求：`{ "nickName": "新昵称", "avatar": "...", "phone": "...", "gender": 1 }`（全部可选）

---

### 轮播图 `/wx/banners`

```
GET /wx/banners          不需要 Token
```
返回：
```json
[
  { "id": 1, "title": "新品上市", "imageUrl": "/images/热菜/经典红烧肉.png", "linkUrl": "", "sort": 1, "status": 1 }
]
```
> 仅返回 status=1 的数据，按 sort 排序

**展示**：`<image src="{{imageBase + item.imageUrl}}" />`

---

### 分类（含菜品）`/wx/categories`

```
GET /wx/categories       不需要 Token
```
返回：
```json
[
  {
    "id": 1, "categoryName": "热菜", "sort": 1,
    "dishes": [
      {
        "id": 1, "dishName": "经典红烧肉", "price": 35.00,
        "image": "/images/热菜/经典红烧肉.png", "sales": 120, "stock": 50,
        "calories": 497, "protein": 7.7, "fat": 52.3, "carbs": 0.9
      }
    ]
  },
  { "id": 2, "categoryName": "汤类", "sort": 2, "dishes": [...] }
]
```
> 只返回已上架菜品，含营养数据（热量/蛋白质/脂肪/碳水化合物，单位均为每100g含量）

---

### 菜品浏览 `/wx/dishes`

| 接口 | 说明 |
|------|------|
| `GET /wx/dishes?categoryId=1` | 按分类查菜品 |
| `GET /wx/dishes/search?keyword=红烧` | 搜索菜品 |
| `GET /wx/dishes/{id}` | 菜品详情（含营养数据） |

---

### 购物车 `/wx/cart`（需要 Token）

| 方法 | 路径 | 请求体 | 说明 |
|------|------|------|------|
| GET | `/wx/cart` | - | 获取列表 |
| POST | `/wx/cart` | `{"dishId":1,"quantity":2}` | 添加（已存在则累加） |
| PUT | `/wx/cart/{id}` | `{"quantity":3}` | 改数量（≤0则删除） |
| DELETE | `/wx/cart/{id}` | - | 删除单项 |
| DELETE | `/wx/cart/clear` | - | 清空 |

GET 返回示例：
```json
[{ "id": 1, "dishId": 1, "dishName": "经典红烧肉", "image": "/images/热菜/经典红烧肉.png", "price": 35.00, "quantity": 2, "totalPrice": 70.00, "stock": 50 }]
```

---

### 收货地址 `/wx/address`（需要 Token）

| 方法 | 路径 | 请求体 | 说明 |
|------|------|------|------|
| GET | `/wx/address` | - | 地址列表 |
| POST | `/wx/address` | `{"receiver":"张三","phone":"138...","addressDetail":"X宿舍","isDefault":1}` | 新增 |
| PUT | `/wx/address/{id}` | `{"receiver":"李四"}` | 编辑（改哪个传哪个） |
| DELETE | `/wx/address/{id}` | - | 删除 |
| PUT | `/wx/address/{id}/default` | - | 设为默认 |

---

### 订单 `/wx/orders`（需要 Token）

#### 提交订单
```
POST /wx/orders
```
请求：`{ "addressId": 1, "remark": "少放辣" }`
返回：`{ "orderId": 10, "orderNo": "WX202606171430001234", "totalAmount": 85.00 }`
> 自动从购物车生成订单明细、扣库存、清空购物车。订单状态初始为 1（待处理）

#### 订单列表
```
GET /wx/orders?page=1&pageSize=10&status=1
```
status：0已取消 / 1待处理 / 2已接单 / 3已完成（不传返回全部）

返回：`{ "total": 10, "list": [...], "page": 1, "pageSize": 10 }`

#### 订单详情
```
GET /wx/orders/{id}
```
返回：`{ "order": {...}, "details": [{ "dishName": "红烧肉", "price": 35, "quantity": 2, "amount": 70 }] }`

#### 取消订单
```
PUT /wx/orders/{id}/cancel
```
> 仅待处理订单可取消

---

### 支付 `/wx/payments`（需要 Token）

```
POST /wx/payments
```
请求：`{ "orderId": 10, "payMethod": "wechat" }`（payMethod：wechat 或 alipay）

返回：`{ "payNo": "PAY202606171430009876", "payAmount": 85.00 }`

---

### 反馈 `/wx/feedbacks`（需要 Token）

| 方法 | 路径 | 请求体 | 说明 |
|------|------|------|------|
| POST | `/wx/feedbacks` | `{"content":"建议增加蔬菜沙拉"}` | 提交反馈 |
| GET | `/wx/feedbacks` | - | 我的反馈列表（含管理员回复与状态） |

---

### 评价 `/wx/comments`（需要 Token）

#### 评价订单
```
POST /wx/comments
```
请求：`{ "orderId": 10, "score": 5, "content": "味道很好！", "photo": "可选" }`
> 仅已完成订单可评价，每单只能评一次

#### 查看评价
```
GET /wx/comments/order/{orderId}
```

---

### ⭐ AI 个性化推荐 `/wx/recommend`（需要 Token）

```
GET /wx/recommend
```
根据用户历史订单+当前时段（早餐/午餐/下午茶/晚餐/夜宵），DeepSeek AI 推荐 3 道菜品。

返回：
```json
{
  "period": "午餐",
  "dishes": [
    { "id": 1, "dishName": "经典红烧肉", "image": "/images/热菜/经典红烧肉.png", "price": 35.00, "categoryName": "热菜", "reason": "您多次点红烧肉，今天搭配汤品更均衡" },
    { "id": 3, "dishName": "番茄牛腩汤", "image": "/images/汤类/番茄牛腩汤.png", "price": 28.00, "categoryName": "汤类", "reason": "午餐搭配热汤暖胃舒适" },
    { "id": 8, "dishName": "扬州炒饭", "image": "/images/米饭/扬州炒饭.png", "price": 18.00, "categoryName": "米饭", "reason": "经典主食，搭配满分" }
  ]
}
```

**首页展示**：做一个横向滚动的"今日推荐"卡片区域

---

### 🥗 AI 膳食营养分析 `/wx/nutrition`（需要 Token）

#### 多轮对话
```
POST /wx/nutrition/chat
```
请求：`{ "message": "我今天点的菜营养怎么样？" }`

返回：
```json
{
  "reply": "您今天点了红烧肉(497千卡)配米饭(380千卡)，总热量约877千卡。脂肪含量偏高，建议搭配蒜泥白肉中的蒜汁解腻，或加一份冰红茶。",
  "hasOrders": true
}
```
> 支持连续追问，系统保留最近 10 轮对话

#### 饮食周报
```
GET /wx/nutrition/report
```
返回：
```json
{
  "orderCount": 5,
  "stats": { "totalCalories": 3500, "totalProtein": 180.5, "totalFat": 150.2, "totalCarbs": 420.0, "topDishes": "经典红烧肉(3次)、扬州炒饭(2次)" },
  "report": "📊 您的7天饮食周报\n\n本周共完成5笔订单。日均热量约500千卡。蛋白质摄入略低于推荐量，建议增加鱼虾豆制品..."
}
```

---

## 四、下单完整流程

```
1. 登录           POST /wx/user/login              → 拿到 token
2. 浏览           GET /wx/categories                → 菜单数据
3. AI推荐          GET /wx/recommend                → 首页推荐卡片
4. 加购物车        POST /wx/cart                    → 多次调用
5. 管理购物车      GET/PUT/DELETE /wx/cart
6. 管理地址        GET/POST /wx/address
7. 下单            POST /wx/orders { addressId }    → 返回 orderNo
8. 支付            POST /wx/payments { orderId }
9. 查看订单        GET /wx/orders?status=1
10. 评价           POST /wx/comments { orderId, score, content }
11. 反馈           POST /wx/feedbacks { content }
12. 营养对话       POST /wx/nutrition/chat
13. 饮食周报       GET /wx/nutrition/report
```

---

## 五、30 个接口速查表

| # | 方法 | 路径 | Token | 说明 |
|:--:|------|------|:--:|------|
| 1 | POST | `/wx/user/login` | ❌ | 登录/注册 |
| 2 | GET | `/wx/user/info` | ✅ | 个人信息 |
| 3 | PUT | `/wx/user/info` | ✅ | 更新信息 |
| 4 | GET | `/wx/banners` | ❌ | 轮播图 |
| 5 | GET | `/wx/categories` | ❌ | 分类+菜品 |
| 6 | GET | `/wx/dishes?categoryId=` | ❌ | 按分类查菜品 |
| 7 | GET | `/wx/dishes/search?keyword=` | ❌ | 搜索菜品 |
| 8 | GET | `/wx/dishes/{id}` | ❌ | 菜品详情 |
| 9 | GET | `/wx/cart` | ✅ | 购物车列表 |
| 10 | POST | `/wx/cart` | ✅ | 加入购物车 |
| 11 | PUT | `/wx/cart/{id}` | ✅ | 改数量 |
| 12 | DELETE | `/wx/cart/{id}` | ✅ | 删单项 |
| 13 | DELETE | `/wx/cart/clear` | ✅ | 清空购物车 |
| 14 | GET | `/wx/address` | ✅ | 地址列表 |
| 15 | POST | `/wx/address` | ✅ | 新增地址 |
| 16 | PUT | `/wx/address/{id}` | ✅ | 编辑地址 |
| 17 | DELETE | `/wx/address/{id}` | ✅ | 删除地址 |
| 18 | PUT | `/wx/address/{id}/default` | ✅ | 设为默认 |
| 19 | POST | `/wx/orders` | ✅ | 提交订单 |
| 20 | GET | `/wx/orders` | ✅ | 订单列表 |
| 21 | GET | `/wx/orders/{id}` | ✅ | 订单详情 |
| 22 | PUT | `/wx/orders/{id}/cancel` | ✅ | 取消订单 |
| 23 | POST | `/wx/payments` | ✅ | 发起支付 |
| 24 | POST | `/wx/feedbacks` | ✅ | 提交反馈 |
| 25 | GET | `/wx/feedbacks` | ✅ | 反馈列表 |
| 26 | POST | `/wx/comments` | ✅ | 评价订单 |
| 27 | GET | `/wx/comments/order/{orderId}` | ❌ | 查看评价 |
| 28 | **GET** | **`/wx/recommend`** | ✅ | **⭐ AI 推荐** |
| 29 | **POST** | **`/wx/nutrition/chat`** | ✅ | **🥗 营养对话** |
| 30 | **GET** | **`/wx/nutrition/report`** | ✅ | **📊 饮食周报** |

---

## 六、图片规则

数据库存相对路径，展示时拼接 imageBase：

```
/image/热菜/经典红烧肉.png
→ http://192.168.214.146:8080/api/images/热菜/经典红烧肉.png
```

---

## 七、联调备忘

1. 微信开发者工具 → **详情 → 本地设置 → 勾选"不校验合法域名"**
2. 两人连同一个 WiFi 或热点
3. 后端同学重连网络后 IP 会变，演示前 `ipconfig` 确认
