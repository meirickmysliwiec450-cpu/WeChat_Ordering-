# 微信点餐系统 - 小程序端完整对接文档

> **Base URL**：`http://192.168.214.146:8080/api`
>
> 演示前重新查 IP：命令行输入 `ipconfig`，找到 IPv4 地址。

---

## 商家后台已完成的全部功能

### 一、基础管理模块

| 模块 | 已实现功能 |
|------|------|
| 🔐 **管理员登录** | JWT Token 认证，拦截器保护所有 `/admin/**` 接口 |
| 📊 **数据仪表盘** | 总用户数、总订单数、总营业额、菜品数量、今日订单数、待处理反馈、订单状态分布 |
| 👥 **用户管理** | 用户列表分页、查看详情、编辑信息、启用/禁用 |
| 📂 **分类管理** | 分类 CRUD、排序管理、新增自动排最后 |
| 🍜 **菜品管理** | 菜品 CRUD、分类筛选、关键词搜索、上下架、手动指定ID防跳号、图片选择器、**AI智能填充营养数据** |
| 📦 **订单管理** | 订单列表分页、状态筛选（已取消/待处理/已接单/已完成）、接单/完成/拒单、订单详情(含明细) |
| 🎨 **轮播图管理** | 轮播图 CRUD、启用/禁用、排序、**图片选择器(直接从 public/images 选取)** |
| 💬 **反馈管理** | 反馈列表分页、状态筛选、管理员回复、标记已解决 |
| ⭐ **评价管理** | 查看顾客对订单的评价（评分星级、文字、图片）、删除不当评价 |
| 💰 **支付记录** | 查看支付流水（流水号、金额、支付方式、时间）、按订单号筛选 |
| 🆕 **超级管理员** | 查看所有管理员列表、注册新管理员、启用/禁用管理员 |

### 二、AI 智能模块

| 模块 | 已实现功能 |
|------|------|
| 🤖 **AI个性化推荐** | 根据用户历史订单 + 当前时段(早/午/晚餐)，调用 DeepSeek 大模型生成"今日推荐"3道菜品+推荐理由，API失败自动兜底推荐 |
| 🥗 **AI膳食营养助手** | 菜品营养数据 → RAG知识库 → DeepSeek 多轮对话，分析膳食搭配，连续点餐后生成饮食周报 |
| 🔬 **AI智能营养填充** | 菜品管理中输入菜名 → 一键调用大模型自动查询热量/蛋白质/脂肪/碳水化合物 |

### 三、数据与基础设施

| 模块 | 已实现功能 |
|------|------|
| 🗄️ **数据库** | 12 张表：admin, user, category, dish, t_order, order_detail, cart, address, payment, feedback, order_comment, t_conversation |
| 🔒 **安全** | JWT 双端认证（Admin 管理端 + Wx 用户端），拦截器自动校验 Token |
| 🖼️ **图片服务** | Spring Boot 映射 `/images/**` 提供静态图片访问，支持小程序的绝对URL访问 |
| 🌐 **跨域** | CORS 全开放，支持小程序跨域请求 |
| 📝 **API文档** | 本文档：30 个接口，包含请求参数、返回示例、小程序代码片段 |

### 四、AI 功能的技术实现

**AI 个性化推荐流程**：
```
用户浏览首页 → GET /wx/recommend
    ↓
查询该用户历史订单（按次数排序）+ 当前时段
    ↓
构建 Prompt："用户喜欢红烧肉(3次)、炒饭(2次)，当前午餐时段，推荐3道菜"
    ↓
调用 DeepSeek API → 返回 JSON { dishes: [{dishName, reason}, ...] }
    ↓
匹配数据库中实际菜品 → 返回完整信息(图片/价格/理由)
```

**AI 营养助手流程**：
```
用户发送消息 → POST /wx/nutrition/chat
    ↓
查询用户今日订单 → 提取菜品营养数据(热量/蛋白/脂肪/碳水)
    ↓
构建 RAG 上下文 = 全部菜品营养库 + 用户今日数据 + 膳食指南参考值
    ↓
拼接历史对话(最近10轮) → 调用 DeepSeek → 返回营养分析建议
    ↓
饮食周报：统计7天营养摄入总量 → AI生成评价+改善建议
```

### 五、商家后台页面一览

| 页面 | 前端文件 | 路由 |
|------|------|------|
| 登录页 | Login.vue | `/login` |
| 仪表盘（含AI推荐预览+营养模拟+周报预览） | Dashboard.vue | `/dashboard` |
| 用户管理 | UserList.vue | `/users` |
| 分类管理 | CategoryList.vue | `/categories` |
| 菜品管理（含AI营养填充） | DishList.vue | `/dishes` |
| 订单管理 | OrderList.vue | `/orders` |
| 订单详情 | OrderDetail.vue | `/orders/:id` |
| 轮播图管理（含图片选择器） | BannerList.vue | `/banners` |
| 反馈管理 | FeedbackList.vue | `/feedbacks` |
| 评价管理 | CommentList.vue | `/comments` |
| 支付记录 | PaymentList.vue | `/payments` |
| 🆕 管理员管理（仅超级管理员可见） | AdminList.vue | `/admins/manage` |

---

## 微信小程序端需要实现的页面

| 页面 | 功能描述 | 所需接口 |
|------|------|------|
| **首页** | 轮播图 + AI今日推荐 + 分类入口 | `/wx/banners` `/wx/recommend` `/wx/categories` |
| **菜单/点餐** | 分类浏览、搜索、菜品详情(含营养)、加购物车 | `/wx/categories` `/wx/dishes` `/wx/dishes/search` `/wx/dishes/{id}` |
| **购物车** | 查看、加减数量、删除、去结算 | `/wx/cart` 全部5个接口 |
| **下单** | 选地址、备注、提交订单 | `/wx/address` 全部5个 + `POST /wx/orders` |
| **订单** | 按状态查看（待处理/已完成等）、详情、取消、支付 | `/wx/orders` 全部4个 + `POST /wx/payments` |
| **评价** | 已完成订单打分+文字+图片 | `/wx/comments` 2个接口 |
| **我的** | 个人信息、我的反馈、营养助手、饮食周报 | `/wx/user` `/wx/feedbacks` `/wx/nutrition` |
| **AI营养助手** | 多轮对话、饮食周报生成 | `POST /wx/nutrition/chat` `GET /wx/nutrition/report` |

---

---

## 第二部分：小程序对接基础配置

### 1. 创建配置文件

在项目根目录创建 `config.js`：

```js
const CONFIG = {
  // 演示前改这个 IP 即可
  baseURL: 'http://192.168.214.146:8080/api',
  // 图片都拼这个前缀
  imageBase: 'http://192.168.214.146:8080/api',
}
export default CONFIG
```

### 2. 封装请求工具

创建 `utils/request.js`：

```js
import CONFIG from '../config'

export function request(options) {
  const token = wx.getStorageSync('token')
  return new Promise((resolve, reject) => {
    wx.request({
      url: CONFIG.baseURL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        // 有 token 就带上
        ...(token ? { 'Authorization': 'Bearer ' + token } : {})
      },
      success(res) {
        if (res.data.code === 200) {
          resolve(res.data.data)
        } else if (res.data.code === 401) {
          wx.removeStorageSync('token')
          wx.reLaunch({ url: '/pages/login/login' })
        } else {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail(err) {
        wx.showToast({ title: '网络错误', icon: 'none' })
        reject(err)
      }
    })
  })
}
```

### 3. 图片显示规则

**所有图片都是相对路径**，展示时拼接 imageBase：

```html
<!-- 轮播图 -->
<image src="{{imageBase + item.imageUrl}}" mode="aspectFill" />

<!-- 菜品图片 -->
<image src="{{imageBase + dish.image}}" mode="aspectFill" />

<!-- 实际效果 -->
<!-- http://192.168.214.146:8080/api/images/热菜/经典红烧肉.png -->
```

---

## 第三部分：30个接口详细说明

---

## 模块一：用户 `/wx/user`（3 个接口）

### 1.1 微信登录 / 自动注册

新用户首次打开小程序 → 调 `wx.login` 拿 code → 调 `wx.getUserProfile` 拿昵称头像 → 调此接口。

```
POST /wx/user/login
不需要 Token
```

**请求参数**：

| 字段 | 类型 | 必填 | 说明 |
|------|------|:---:|------|
| openId | String | 是 | 微信授权 openId，测试阶段可填 "test_openid_123" |
| nickName | String | 否 | 微信昵称 |
| avatar | String | 否 | 头像 URL |
| gender | Integer | 否 | 性别：0未知 1男 2女 |

**请求示例**：
```json
{
  "openId": "oABC123xyz",
  "nickName": "小明的微信昵称",
  "avatar": "https://thirdwx.qlogo.cn/xxx",
  "gender": 1
}
```

**返回数据**：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "user": {
      "id": 1,
      "openId": "oABC123xyz",
      "nickName": "小明的微信昵称",
      "avatar": "https://thirdwx.qlogo.cn/xxx",
      "gender": 1,
      "phone": null,
      "status": 1
    }
  }
}
```

**小程序代码示例**：
```js
import { request } from '../../utils/request'

async function login(openId, nickName, avatar) {
  const data = await request({
    url: '/wx/user/login',
    method: 'POST',
    data: { openId, nickName, avatar, gender: 1 }
  })
  // 存 token
  wx.setStorageSync('token', data.token)
  // 存用户信息
  wx.setStorageSync('userInfo', data.user)
  return data
}
```

**重要**：
- 同一个 openId 第一次调用是**注册**，后续调用是**登录**
- Token 有效期 7 天，存到 `wx.setStorageSync`
- **所有后续接口请求头必须带** `Authorization: Bearer <token>`

---

### 1.2 获取个人信息

```
GET /wx/user/info
需要 Token
```

**返回**：
```json
{
  "code": 200,
  "data": {
    "id": 1, "openId": "oABC123xyz", "nickName": "小明",
    "avatar": "...", "gender": 1, "phone": "13800138000", "status": 1
  }
}
```

---

### 1.3 更新个人信息

```
PUT /wx/user/info
需要 Token
```

**请求参数**：全部可选，改哪个传哪个
```json
{
  "nickName": "新昵称",
  "avatar": "新头像URL",
  "phone": "13800138000",
  "gender": 1
}
```
**返回**：`{ "code": 200, "message": "成功", "data": null }`

---

## 模块二：轮播图 `/wx/banners`（1 个接口）

### 2.1 获取首页轮播图

```
GET /wx/banners
不需要 Token
```

**返回**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "title": "新品上市",
      "imageUrl": "/images/热菜/经典红烧肉.png",
      "linkUrl": "",
      "sort": 1,
      "status": 1
    },
    {
      "id": 2,
      "title": "招牌推荐",
      "imageUrl": "/images/面食/招牌牛肉面.png",
      "linkUrl": "",
      "sort": 2,
      "status": 1
    }
  ]
}
```

**说明**：只返回 `status=1` 的数据，按 `sort` 从小到大排序。

**小程序代码**：
```html
<!-- 首页轮播图 -->
<swiper indicator-dots autoplay interval="3000" circular>
  <swiper-item wx:for="{{banners}}" wx:key="id">
    <image src="{{imageBase + item.imageUrl}}" mode="aspectFill" />
  </swiper-item>
</swiper>
```
```js
import { request } from '../../utils/request'
import CONFIG from '../../config'

Page({
  data: { banners: [], imageBase: CONFIG.imageBase },
  onLoad() {
    request({ url: '/wx/banners' }).then(data => {
      this.setData({ banners: data })
    })
  }
})
```

---

## 模块三：分类+菜品 `/wx/categories`（1 个接口）

### 3.1 获取所有分类及其菜品

```
GET /wx/categories
不需要 Token
```

**返回**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "categoryName": "热菜",
      "sort": 1,
      "dishes": [
        {
          "id": 1,
          "dishName": "经典红烧肉",
          "price": 35.00,
          "image": "/images/热菜/经典红烧肉.png",
          "description": "五花肉慢炖",
          "sales": 120,
          "stock": 50,
          "status": 1,
          "calories": 497,
          "protein": 7.7,
          "fat": 52.3,
          "carbs": 0.9
        },
        { "id": 2, "dishName": "鸡翅", "price": 28.00, "image": "/images/热菜/鸡翅.png", "calories": 240, "protein": 18.0, "fat": 16.5, "carbs": 5.2 }
      ]
    },
    {
      "id": 2,
      "categoryName": "汤类",
      "sort": 2,
      "dishes": [
        { "id": 3, "dishName": "番茄牛腩汤", "price": 28.00, "image": "/images/汤类/番茄牛腩汤.png", "calories": 150, "protein": 12.0, "fat": 8.0, "carbs": 10.0 },
        { "id": 4, "dishName": "腌笃鲜", "price": 32.00, "image": "/images/汤类/腌笃鲜.png", "calories": 180, "protein": 15.0, "fat": 10.0, "carbs": 8.0 }
      ]
    },
    { "id": 3, "categoryName": "冷菜", "sort": 3, "dishes": [{"id": 5, "dishName": "蒜泥白肉", "price": 22.00, "image": "/images/冷菜/蒜泥白肉.png", "calories": 280, "protein": 14.0, "fat": 22.0, "carbs": 6.0}] },
    { "id": 4, "categoryName": "米饭", "sort": 4, "dishes": [{"id": 6, "dishName": "扬州炒饭", "price": 18.00, "image": "/images/米饭/扬州炒饭.png", "calories": 380, "protein": 12.0, "fat": 14.0, "carbs": 50.0}, {"id": 7, "dishName": "广式腊味煲仔饭", "price": 25.00, "image": "/images/米饭/广式腊味煲仔饭.png", "calories": 420, "protein": 16.0, "fat": 18.0, "carbs": 52.0}] },
    { "id": 5, "categoryName": "面食", "sort": 5, "dishes": [{"id": 8, "dishName": "招牌牛肉面", "price": 22.00, "image": "/images/面食/招牌牛肉面.png", "calories": 350, "protein": 15.0, "fat": 10.0, "carbs": 48.0}, {"id": 9, "dishName": "饺子", "price": 16.00, "image": "/images/面食/饺子.png", "calories": 250, "protein": 10.0, "fat": 8.0, "carbs": 35.0}, {"id": 10, "dishName": "馒头", "price": 3.00, "image": "/images/面食/馒头.png", "calories": 220, "protein": 7.0, "fat": 1.0, "carbs": 44.0}] },
    { "id": 6, "categoryName": "饮品", "sort": 6, "dishes": [{"id": 11, "dishName": "冰红茶", "price": 8.00, "image": "/images/饮品/冰红茶.png", "calories": 42, "protein": 0, "fat": 0, "carbs": 10.5}, {"id": 12, "dishName": "多肉葡萄冰萃", "price": 15.00, "image": "/images/饮品/多肉葡萄冰萃.png", "calories": 65, "protein": 0.5, "fat": 0, "carbs": 16.0}, {"id": 13, "dishName": "美年达", "price": 6.00, "image": "/images/饮品/美年达.png", "calories": 48, "protein": 0, "fat": 0, "carbs": 12.0}] }
  ]
}
```

**菜品字段说明**：
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Number | 菜品 ID |
| dishName | String | 菜品名称 |
| price | Number | 价格（元） |
| image | String | 图片相对路径 |
| sales | Number | 累计销量 |
| stock | Number | 库存 |
| calories | Number | 热量（千卡/100g） |
| protein | Number | 蛋白质（g/100g） |
| fat | Number | 脂肪（g/100g） |
| carbs | Number | 碳水化合物（g/100g） |

**小程序用法**：本接口一次返回所有数据，可用来做左右双栏菜单页（左栏分类、右栏菜品）。

```html
<!-- 菜单页 -->
<scroll-view class="left-category" scroll-y>
  <view wx:for="{{categories}}" wx:key="id"
        class="{{currentId === item.id ? 'active' : ''}}"
        bindtap="switchCategory" data-id="{{item.id}}">
    {{item.categoryName}}
  </view>
</scroll-view>

<scroll-view class="right-dishes" scroll-y>
  <view wx:for="{{currentDishes}}" wx:key="id" class="dish-card">
    <image src="{{imageBase + item.image}}" mode="aspectFill" />
    <view class="dish-info">
      <text class="dish-name">{{item.dishName}}</text>
      <text class="dish-price">¥{{item.price}}</text>
      <text class="dish-sales">已售 {{item.sales}}</text>
      <text class="dish-nutrition">🔥{{item.calories}}千卡 | 蛋白{{item.protein}}g</text>
      <button bindtap="addToCart" data-dish="{{item}}">+ 加入购物车</button>
    </view>
  </view>
</scroll-view>
```

---

## 模块四：菜品浏览 `/wx/dishes`（3 个接口）

### 4.1 按分类获取菜品

```
GET /wx/dishes?categoryId=1
不需要 Token
```

**返回**：JSON 数组，只包含该分类的上架菜品
```json
{
  "code": 200,
  "data": [
    { "id": 1, "dishName": "经典红烧肉", "price": 35.00, "image": "/images/热菜/经典红烧肉.png", "calories": 497, "protein": 7.7, "fat": 52.3, "carbs": 0.9 },
    { "id": 2, "dishName": "鸡翅", "price": 28.00, "image": "/images/热菜/鸡翅.png", "calories": 240, "protein": 18.0, "fat": 16.5, "carbs": 5.2 }
  ]
}
```

### 4.2 搜索菜品

```
GET /wx/dishes/search?keyword=红烧
不需要 Token
```

**返回**：JSON 数组，菜名模糊匹配
```json
{
  "code": 200,
  "data": [
    { "id": 1, "dishName": "经典红烧肉", "price": 35.00, "image": "/images/热菜/经典红烧肉.png" }
  ]
}
```

### 4.3 菜品详情

```
GET /wx/dishes/{id}
不需要 Token
```

**返回**：
```json
{
  "code": 200,
  "data": {
    "id": 1, "dishName": "经典红烧肉", "categoryId": 1,
    "price": 35.00, "image": "/images/热菜/经典红烧肉.png",
    "description": "五花肉慢炖至酥烂",
    "sales": 120, "stock": 50,
    "calories": 497, "protein": 7.7, "fat": 52.3, "carbs": 0.9
  }
}
```

---

## 模块五：购物车 `/wx/cart`（5 个接口）需 Token

### 5.1 查看购物车

```
GET /wx/cart
```

**返回**：
```json
{
  "code": 200,
  "data": [
    {
      "id": 1, "dishId": 1, "dishName": "经典红烧肉",
      "image": "/images/热菜/经典红烧肉.png",
      "price": 35.00, "quantity": 2, "totalPrice": 70.00, "stock": 50
    }
  ]
}
```

### 5.2 添加到购物车

```
POST /wx/cart
```

**请求**：`{ "dishId": 1, "quantity": 2 }`

**返回**：`{ "code": 200, "message": "成功" }`

**规则**：如果购物车已有该菜品，自动累加数量

### 5.3 修改数量

```
PUT /wx/cart/{购物车项id}
```

**请求**：`{ "quantity": 3 }`

**规则**：数量 ≤ 0 则自动删除该项

### 5.4 删除单项

```
DELETE /wx/cart/{购物车项id}
```

### 5.5 清空购物车

```
DELETE /wx/cart/clear
```

**小程序购物车页面代码示例**：
```js
Page({
  data: { cartList: [], totalPrice: 0 },
  onShow() {
    this.loadCart()
  },
  async loadCart() {
    const list = await request({ url: '/wx/cart' })
    const total = list.reduce((sum, item) => sum + item.totalPrice, 0)
    this.setData({ cartList: list, totalPrice: total.toFixed(2) })
  },
  async changeQty(item, delta) {
    const newQty = item.quantity + delta
    if (newQty <= 0) {
      await request({ url: `/wx/cart/${item.id}`, method: 'DELETE' })
    } else {
      await request({ url: `/wx/cart/${item.id}`, method: 'PUT', data: { quantity: newQty } })
    }
    this.loadCart()
  },
  async submitOrder(addressId, remark) {
    const result = await request({
      url: '/wx/orders', method: 'POST',
      data: { addressId, remark }
    })
    wx.showToast({ title: '下单成功，订单号：' + result.orderNo })
    this.loadCart()
  }
})
```

---

## 模块六：收货地址 `/wx/address`（5 个接口）需 Token

| 方法 | 路径 | 请求体 | 返回 |
|------|------|------|------|
| GET | `/wx/address` | 无 | 地址数组 |
| POST | `/wx/address` | `{"receiver":"张三","phone":"13800138000","addressDetail":"3号楼101","isDefault":1}` | 成功 |
| PUT | `/wx/address/{id}` | `{"receiver":"李四","isDefault":0}` | 成功 |
| DELETE | `/wx/address/{id}` | 无 | 成功 |
| PUT | `/wx/address/{id}/default` | 无 | 成功 |

**地址数据结构**：
```json
{
  "id": 1, "userId": 1,
  "receiver": "张三", "phone": "13800138000",
  "addressDetail": "XX大学3号楼101",
  "isDefault": 1
}
```

---

## 模块七：订单 `/wx/orders`（4 个接口）需 Token

### 7.1 提交订单

```
POST /wx/orders
```

**请求**：`{ "addressId": 1, "remark": "少放辣" }`

**返回**：
```json
{ "orderId": 10, "orderNo": "WX202606171430001234", "totalAmount": 85.00 }
```

**后台自动完成**：购物车 → 订单 + 订单明细、扣库存、增销量、清空购物车
**订单初始状态**：1（待处理）

### 7.2 我的订单列表

```
GET /wx/orders?page=1&pageSize=10&status=1
```

| 参数 | 必填 | 说明 |
|------|:---:|------|
| page | 否 | 默认 1 |
| pageSize | 否 | 默认 10 |
| status | 否 | 0已取消/1待处理/2已接单/3已完成，不传返回全部 |

**返回**：
```json
{
  "code": 200,
  "data": {
    "total": 5, "page": 1, "pageSize": 10,
    "list": [
      {
        "id": 10, "orderNo": "WX202606171430001234",
        "userId": 1, "totalAmount": 85.00, "payAmount": 85.00,
        "payStatus": 0, "orderStatus": 1, "remark": "少放辣",
        "receiver": "张三", "receiverPhone": "13800138000",
        "createTime": "2026-06-17T14:30:00"
      }
    ]
  }
}
```

### 7.3 订单详情

```
GET /wx/orders/{id}
```

**返回**：
```json
{
  "code": 200,
  "data": {
    "order": {
      "id": 10, "orderNo": "WX202606171430001234",
      "totalAmount": 85.00, "payAmount": 85.00,
      "payStatus": 0, "orderStatus": 1, "remark": "少放辣",
      "receiver": "张三", "receiverPhone": "13800138000",
      "createTime": "2026-06-17T14:30:00"
    },
    "details": [
      { "dishName": "经典红烧肉", "price": 35.00, "quantity": 2, "amount": 70.00 },
      { "dishName": "冰红茶", "price": 8.00, "quantity": 1, "amount": 8.00 }
    ]
  }
}
```

### 7.4 取消订单

```
PUT /wx/orders/{id}/cancel
```
**规则**：仅 status=1（待处理）的订单可取消，取消后状态变为 0

---

## 模块八：支付 `/wx/payments`（1 个接口）需 Token

```
POST /wx/payments
```
**请求**：`{ "orderId": 10, "payMethod": "wechat" }`
- `payMethod`：`"wechat"` 或 `"alipay"`

**返回**：`{ "payNo": "PAY202606171430009876", "payAmount": 85.00 }`

**说明**：模拟支付，不真扣钱。支付成功后订单 `payStatus` 自动变为 1

---

## 模块九：反馈 `/wx/feedbacks`（2 个接口）需 Token

### 9.1 提交反馈

```
POST /wx/feedbacks
```
**请求**：`{ "content": "建议增加蔬菜沙拉" }`

### 9.2 我的反馈列表

```
GET /wx/feedbacks
```
**返回**：`[{ "id": 1, "userId": 1, "content": "建议增加蔬菜沙拉", "replyContent": "好的已记录", "status": 1, "createTime": "..." }]`
- status：0未处理 / 1已回复 / 2已解决
- `replyContent`：管理员在后台回复后会填充该字段

---

## 模块十：评价 `/wx/comments`（2 个接口）需 Token

### 10.1 评价已完成订单

```
POST /wx/comments
```
**请求**：`{ "orderId": 10, "score": 5, "content": "味道很好！", "photo": "/images/xxx.jpg" }`
- score：1-5 星评分
- photo：可选
- 仅 status=3（已完成）的订单可评价，每单只能评一次

### 10.2 查看评价

```
GET /wx/comments/order/{orderId}
```

---

## 模块十一：⭐ AI 个性化推荐 `/wx/recommend`（1 个接口）需 Token

### 11.1 今日为你推荐

```
GET /wx/recommend
```

**功能**：根据用户历史订单 + 当前时段，DeepSeek AI 推荐 3 道菜品

**返回**：
```json
{
  "code": 200,
  "data": {
    "period": "午餐",
    "userName": "",
    "dishes": [
      {
        "id": 1,
        "dishName": "经典红烧肉",
        "image": "/images/热菜/经典红烧肉.png",
        "price": 35.00,
        "sales": 120,
        "categoryName": "热菜",
        "reason": "您多次点红烧肉，今天搭配汤品更加均衡"
      },
      {
        "id": 3,
        "dishName": "番茄牛腩汤",
        "image": "/images/汤类/番茄牛腩汤.png",
        "price": 28.00,
        "sales": 85,
        "categoryName": "汤类",
        "reason": "午餐搭配热汤暖胃舒适"
      },
      {
        "id": 5,
        "dishName": "扬州炒饭",
        "image": "/images/米饭/扬州炒饭.png",
        "price": 18.00,
        "sales": 200,
        "categoryName": "米饭",
        "reason": "经典主食，搭配菜品满分"
      }
    ]
  }
}
```

**时段说明**：
- 6:00-9:59 → 早餐
- 10:00-13:59 → 午餐
- 14:00-16:59 → 下午茶
- 17:00-20:59 → 晚餐
- 21:00-5:59 → 夜宵

**若 API 调用失败**：返回中包含 `"fallback": true`，使用基于销量和偏好的兜底推荐

**小程序首页展示示例**：
```html
<!-- 首页"今日推荐" -->
<view class="recommend-section">
  <view class="section-title">🤖 今日为你推荐 · {{period}}</view>
  <scroll-view scroll-x class="recommend-scroll">
    <view class="recommend-card" wx:for="{{recommends}}" wx:key="id">
      <image src="{{imageBase + item.image}}" mode="aspectFill" />
      <text class="card-name">{{item.dishName}}</text>
      <text class="card-price">¥{{item.price}}</text>
      <text class="card-reason">💡 {{item.reason}}</text>
    </view>
  </scroll-view>
</view>
```

---

## 模块十二：🥗 AI 膳食营养分析 `/wx/nutrition`（2 个接口）需 Token

### 12.1 多轮对话

```
POST /wx/nutrition/chat
```

**功能**：DeepSeek AI 基于 RAG 知识库（菜品营养数据），回答饮食相关问题，支持多轮对话

**请求**：`{ "message": "我今天点的红烧肉和米饭搭配合理吗？" }`

**返回**：
```json
{
  "code": 200,
  "data": {
    "reply": "经典红烧肉（497千卡/100g，脂肪52.3g）搭配米饭（380千卡/100g）是经典组合，但脂肪含量偏高...建议搭配一份蔬菜或冷菜如蒜泥白肉中的蒜汁来解腻，同时增加膳食纤维的摄入。",
    "hasOrders": true
  }
}
```

**对话历史**：系统保留最近 10 轮对话上下文，可以连续追问：
```
用户：那加点什么比较好？
AI：建议加点冰红茶（42千卡，0脂肪）解腻，或番茄牛腩汤（150千卡）补充蔬菜...
```

**小程序实现**：
```html
<!-- 营养助手页面 -->
<view class="chat-container">
  <scroll-view scroll-y class="chat-history">
    <view wx:for="{{messages}}" wx:key="index"
          class="{{item.role === 'user' ? 'msg-user' : 'msg-ai'}}">
      <text>{{item.content}}</text>
    </view>
  </scroll-view>
  <view class="chat-input">
    <input value="{{inputMsg}}" bindinput="onInput" placeholder="问营养问题..." />
    <button bindtap="sendMsg">发送</button>
  </view>
</view>
```
```js
Page({
  data: { messages: [], inputMsg: '' },
  onInput(e) { this.setData({ inputMsg: e.detail.value }) },
  async sendMsg() {
    const msg = this.data.inputMsg.trim()
    if (!msg) return
    this.setData({
      messages: [...this.data.messages, { role: 'user', content: msg }],
      inputMsg: ''
    })
    const res = await request({
      url: '/wx/nutrition/chat', method: 'POST',
      data: { message: msg }
    })
    this.setData({
      messages: [...this.data.messages, { role: 'assistant', content: res.reply }]
    })
  }
})
```

### 12.2 饮食周报

```
GET /wx/nutrition/report
```

**功能**：统计近 7 天已完成订单的营养摄入总量，DeepSeek AI 生成周报

**返回**：
```json
{
  "code": 200,
  "data": {
    "orderCount": 5,
    "stats": {
      "totalCalories": 3500,
      "totalProtein": 180.5,
      "totalFat": 150.2,
      "totalCarbs": 420.0,
      "topDishes": "经典红烧肉(3次)、扬州炒饭(2次)"
    },
    "report": "📊 您的7天饮食周报\n\n本周共完成5笔订单。日均热量约500千卡，属于正常范围。蛋白质摄入(日均25.8g)略低于推荐量(60g/日)，建议增加鱼虾、豆制品等优质蛋白。脂肪摄入(日均21.5g)在合理范围。\n\n1️⃣ 增加蛋白质：点一份鸡翅或牛肉面补充蛋白质\n2️⃣ 增加蔬菜：建议冷菜或汤品搭配\n3️⃣ 控制单一菜品：红烧肉出现频率较高，可尝试鱼香肉丝等不同口味"
  }
}
```

**小程序展示位置**："我的"页面或"营养分析"页面，加一个"生成周报"按钮

---

## 第四部分：完整下单流程

```
用户打开小程序
  │
  ├─ 1. wx.login → 拿 openId → POST /wx/user/login → 获取 token
  │
  ├─ 2. GET /wx/banners → 首页轮播图
  ├─ 3. GET /wx/categories → 菜单页
  ├─ 4. GET /wx/recommend → 首页 AI 推荐卡片
  │
  ├─ 5. 浏览菜品，点"加入购物车" → POST /wx/cart
  │
  ├─ 6. 多次添加 → 进入购物车页 → GET /wx/cart 查看
  ├─    加减数量 → PUT /wx/cart/{id}
  ├─    删除    → DELETE /wx/cart/{id}
  │
  ├─ 7. 收货地址 → GET/POST /wx/address
  │
  ├─ 8. 提交订单 → POST /wx/orders { addressId, remark }
  │       → 返回 orderNo
  │
  ├─ 9. 模拟支付 → POST /wx/payments { orderId, payMethod }
  │
  ├─ 10. 查看订单 → GET /wx/orders?status=1
  │     订单详情 → GET /wx/orders/{id}
  │     取消     → PUT /wx/orders/{id}/cancel
  │
  ├─ 11. 评价    → POST /wx/comments { orderId, score, content }
  │
  ├─ 12. 反馈    → POST /wx/feedbacks { content }
  │     查看    → GET /wx/feedbacks
  │
  ├─ 13. 营养助手 → POST /wx/nutrition/chat { message }
  │     饮食周报 → GET /wx/nutrition/report
  │
  └─ 14. 个人信息 → GET/PUT /wx/user/info
```

---

## 第五部分：接口速查表（30个）

| 序号 | 方法 | 路径 | 需Token | 说明 |
|:---:|------|------|:---:|------|
| 1 | POST | `/wx/user/login` | ❌ | 登录/注册 |
| 2 | GET | `/wx/user/info` | ✅ | 获取个人信息 |
| 3 | PUT | `/wx/user/info` | ✅ | 更新个人信息 |
| 4 | GET | `/wx/banners` | ❌ | 轮播图列表 |
| 5 | GET | `/wx/categories` | ❌ | 分类+菜品 |
| 6 | GET | `/wx/dishes?categoryId=` | ❌ | 按分类查菜品 |
| 7 | GET | `/wx/dishes/search?keyword=` | ❌ | 搜索菜品 |
| 8 | GET | `/wx/dishes/{id}` | ❌ | 菜品详情 |
| 9 | GET | `/wx/cart` | ✅ | 查看购物车 |
| 10 | POST | `/wx/cart` | ✅ | 添加到购物车 |
| 11 | PUT | `/wx/cart/{id}` | ✅ | 修改数量 |
| 12 | DELETE | `/wx/cart/{id}` | ✅ | 删除单项 |
| 13 | DELETE | `/wx/cart/clear` | ✅ | 清空购物车 |
| 14 | GET | `/wx/address` | ✅ | 地址列表 |
| 15 | POST | `/wx/address` | ✅ | 新增地址 |
| 16 | PUT | `/wx/address/{id}` | ✅ | 编辑地址 |
| 17 | DELETE | `/wx/address/{id}` | ✅ | 删除地址 |
| 18 | PUT | `/wx/address/{id}/default` | ✅ | 设为默认 |
| 19 | POST | `/wx/orders` | ✅ | 提交订单 |
| 20 | GET | `/wx/orders?page&pageSize&status` | ✅ | 订单列表 |
| 21 | GET | `/wx/orders/{id}` | ✅ | 订单详情 |
| 22 | PUT | `/wx/orders/{id}/cancel` | ✅ | 取消订单 |
| 23 | POST | `/wx/payments` | ✅ | 发起支付 |
| 24 | POST | `/wx/feedbacks` | ✅ | 提交反馈 |
| 25 | GET | `/wx/feedbacks` | ✅ | 反馈列表 |
| 26 | POST | `/wx/comments` | ✅ | 评价订单 |
| 27 | GET | `/wx/comments/order/{orderId}` | ❌ | 查看评价 |
| 28 | **GET** | **`/wx/recommend`** | ✅ | **⭐ AI推荐** |
| 29 | **POST** | **`/wx/nutrition/chat`** | ✅ | **🥗 AI营养对话** |
| 30 | **GET** | **`/wx/nutrition/report`** | ✅ | **📊 饮食周报** |

---

## 第六部分：联调检查清单

- [ ] 两人连同一个 WiFi 或热点
- [ ] 后端 `ipconfig` 确认 IP，队友改 `config.js` 里的 IP
- [ ] 微信开发者工具 → 详情 → 本地设置 → 勾选"不校验合法域名"
- [ ] 先测试 `GET /wx/banners`，确认能拿到数据
- [ ] 测试 `POST /wx/user/login`，拿到 token
- [ ] 后续测试需带 token 的接口
- [ ] 确认图片能正常显示
