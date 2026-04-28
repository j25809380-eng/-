# FitNote 第二阶段说明

## 本阶段目标

第二阶段重点完成两件事：

- 小程序由 mock 优先切换为真实接口优先，保留自动回退 mock 能力
- 增加后台管理系统第一版，用于答辩展示“管理端 + 数据端 + 小程序端”完整闭环

## 已完成内容

### 1. 小程序联调增强

- 请求层支持按顺序尝试多个本地端口
- 默认优先请求真实后端
- 后端不可达时自动回退 mock 数据
- 计划页切换为真实详情加载
- 社区发布切换为真实接口调用

### 2. 后端功能增强

- 增加 `personal_record` 实体与仓库
- 训练保存时自动更新个人记录
- 增强分析接口：
  - `GET /api/analytics/overview`
  - `GET /api/analytics/personal-records`
- 增加自定义计划接口：
  - `GET /api/plans/mine`
  - `POST /api/plans/custom`

### 3. 后台管理系统

静态管理端页面位于：

- `backend/src/main/resources/static/admin/index.html`

启动后访问：

- `http://127.0.0.1:8080/admin`
- 或 `http://127.0.0.1:8081/admin`

管理接口包括：

- `GET /api/admin/dashboard`
- `GET /api/admin/users`
- `GET /api/admin/workouts`
- `GET /api/admin/posts`
- `PUT /api/admin/posts/{id}/audit`
- `GET /api/admin/plans`

## 适合答辩展示的顺序

1. 打开小程序首页，展示训练入口与计划页。
2. 进入训练记录页，演示记录动作与保存训练。
3. 展示训练完成页和历史统计页。
4. 展示社区页和 AI 助手页。
5. 打开后台管理端，展示总览、用户、训练数据、社区审核与计划管理。

## 下一阶段建议

- 小程序新增“自定义计划创建页”
- 增加社区评论与点赞接口
- 增加月报导出与图表组件
- 新增管理员登录与权限控制
