# FitNote 小程序页面与接口对应表

## 页面总览

| 页面路径 | 对应 Stitch | 页面职责 | 主要接口 |
| --- | --- | --- | --- |
| `/pages/login/index` | 新增 | 登录与首次进入 | `POST /auth/wechat-login` |
| `/pages/home/index` | `_6` | 首页、今日状态、训练入口 | `GET /dashboard/home` |
| `/pages/plan/index` | `_1` | 训练计划探索 | `GET /plans` `GET /plans/{id}` `GET /plans/mine` |
| `/pages/plan-create/index` | 新增 | 创建自定义训练计划 | `POST /plans/custom` |
| `/pages/exercise/index` | `_8` | 动作库检索 | `GET /exercises` |
| `/pages/exercise-detail/index` | `_3` | 动作详情 | `GET /exercises/{id}` |
| `/pages/history/index` | `_4` | 历史训练与统计 | `GET /workouts/history` `GET /analytics/overview` `GET /reports/monthly` |
| `/pages/workout-editor/index` | 新增 | 新增训练记录 | `POST /workouts` `GET /exercises` |
| `/pages/workout-complete/index` | `_5` | 训练完成与分享 | `GET /workouts/{id}` |
| `/pages/ai/index` | `ai` | AI 助手问答入口 | `GET /ai/prompts` |
| `/pages/community/index` | `_9` | 社区动态流 | `GET /community/posts` `POST /community/posts` `POST /community/posts/{id}/like` `GET /community/posts/{id}/comments` `POST /community/posts/{id}/comments` |
| `/pages/ranking/index` | `_2` | 排行榜 | `GET /analytics/rankings` |
| `/pages/profile/index` | 新增 | 用户资料、目标设置、头像上传 | `GET /users/me` `PUT /users/me/profile` `POST /files/avatar` |

## 第一阶段已对接接口

### 1. 用户系统

- `POST /api/auth/wechat-login`
- `GET /api/users/me`
- `PUT /api/users/me/profile`
- `POST /api/files/avatar`

### 2. 训练计划

- `GET /api/dashboard/home`
- `GET /api/plans`
- `GET /api/plans/{id}`

### 3. 动作库

- `GET /api/exercises`
- `GET /api/exercises/{id}`

### 4. 训练记录

- `GET /api/workouts/history`
- `POST /api/workouts`
- `GET /api/workouts/{id}`

### 5. 数据分析

- `GET /api/analytics/overview`
- `GET /api/analytics/rankings`

### 6. 社区

- `GET /api/community/posts`
- `POST /api/community/posts`

### 7. AI 助手

- `GET /api/ai/prompts`

## 第二阶段新增接口

### 1. 训练计划增强

- `GET /api/plans/mine`
- `POST /api/plans/custom`

### 2. 数据分析增强

- `GET /api/analytics/personal-records`

### 3. 后台管理系统

- `GET /api/admin/dashboard`
- `GET /api/admin/users`
- `GET /api/admin/workouts`
- `GET /api/admin/posts`
- `PUT /api/admin/posts/{id}/audit`
- `GET /api/admin/plans`

## 第三阶段新增接口

### 1. 自定义计划创建页

- `POST /api/plans/custom`

### 2. 社区互动增强

- `POST /api/community/posts/{id}/like`
- `GET /api/community/posts/{id}/comments`
- `POST /api/community/posts/{id}/comments`

### 3. 月度报告

- `GET /api/reports/monthly`

### 4. 后台管理员鉴权

- `POST /api/admin/auth/login`
- `GET /api/admin/auth/me`

## 第四阶段新增接口

### 1. 用户资料增强

- `POST /api/files/avatar`
- `POST /api/files/upload`

### 2. 后台数据大屏

- `GET /api/admin/big-screen`

### 3. 后台审核日志

- `GET /api/admin/audit-logs`

## 后续预留接口

- `DELETE /api/workouts/{id}`
- `PUT /api/workouts/{id}`
- `POST /api/workouts/{id}/finish`
- `POST /api/community/posts/{id}/collect`
- `GET /api/reports/monthly/export`
- `POST /api/files/upload`
