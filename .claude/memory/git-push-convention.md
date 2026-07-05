---
name: git-push-convention
description: Git分支策略和提交备注语言约定
metadata: 
  node_type: memory
  type: project
  originSessionId: cbd9f9dd-1f31-4118-8cca-061cf1962982
---

# Git 分支与推送约定

## 分支策略
- **master**: 存放前端代码 (`wx-order-frontend-user1/`)
- **backend-user1**: 存放后端代码 (`wx-order-ssm-back/`)
- 远程仓库: `https://github.com/meirickmysliwiec450-cpu/WeChat_Ordering-.git`

## 提交备注语言
所有 git commit 必须使用**中文**写备注信息，不要使用英文。

**Why:** 用户明确要求 push 的时候修改地方写中文备注，方便团队成员查看和理解提交历史。

**How to apply:** 每次 git commit 时，`-m` 参数使用中文描述修改内容，例如：
- `git commit -m "新增菜品管理Controller"`
- `git commit -m "修复订单状态更新逻辑"`
- `git commit -m "前端新增轮播图管理页面"`
