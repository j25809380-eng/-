# FitNote

FitNote 是一个面向毕业设计答辩的健身记录微信小程序项目。  
当前版本已完成真实后端接入、MySQL 持久化、月报导出、文件上传、社区审核与后台大屏。

## 技术栈

- 小程序前端：微信小程序原生
- 后端：Spring Boot 3 + Spring Security + JPA
- 鉴权：JWT（用户端与管理员端）
- 数据库：MySQL 8（保留 `demo` profile 用于离线演示）
- 文件存储：本地 `uploads/` 目录（头像、社区图片、报表导出）

## 项目结构

```text
stitch_onyx_fitness_tracker/
├─ backend/                     Spring Boot 后端
├─ miniprogram/                 微信小程序工程
├─ docs/                        设计与答辩文档
├─ docker-compose.yml           MySQL + Adminer
└─ README.md
```

## 已完成能力（收官版）

- 真实微信登录 `code2Session`（支持 `WECHAT_APP_ID` / `WECHAT_APP_SECRET`）
- 小程序请求层禁用 mock 回退，默认真实接口
- 训练记录、计划、社区、统计全部走 MySQL 持久化
- 历史页接入图表组件（体重趋势、训练量趋势）
- 月报导出接口 `GET /api/reports/monthly/export`（CSV）
- 文件上传：通用上传、头像上传、社区图片发布
- 后台管理：用户/训练/社区审核/计划管理
- 审核日志增强：前状态、后状态、操作者、内容快照、扩展信息
- 后台大屏增强：审核状态分布、近期开审日志
- 全页面返回按钮已补齐（小程序）

## 后端运行

### 1. 准备 MySQL

执行：

```sql
SOURCE backend/src/main/resources/sql/schema-mysql.sql;
```

或使用 Docker：

```powershell
docker compose up -d
```

### 2. 配置数据库

编辑 `backend/src/main/resources/application-mysql.yml`：

- `spring.datasource.url`
- `spring.datasource.username`
- `spring.datasource.password`

### 3. 配置微信真实登录（生产/答辩建议）

设置环境变量：

```powershell
$env:WECHAT_APP_ID="你的小程序AppID"
$env:WECHAT_APP_SECRET="你的小程序AppSecret"
```

默认 `application.yml` 已启用：

- `spring.profiles.active: mysql`
- `app.wechat.mock-login: false`

### 4. 启动后端

```powershell
$env:JAVA_HOME="C:\Program Files\Java\jdk-21"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run
```

如果 8080 端口被占用：

```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

### 5. demo 模式（仅离线演示）

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=demo"
```

> `demo` profile 会注入演示数据；`mysql` profile 使用真实持久化数据。

## 小程序运行

1. 微信开发者工具导入项目根目录
2. `miniprogramRoot` 设置为 `miniprogram`
3. 确保后端可访问：
   - `http://127.0.0.1:8080/api`
   - 或 `http://127.0.0.1:8081/api`
4. 点击登录按钮，走 `wx.login -> /auth/wechat-login`

前端请求配置：`miniprogram/utils/config.js`

- `useMock: false`
- `fallbackToMock: false`

## 管理后台

- 控制台：`http://127.0.0.1:8080/admin`
- 大屏：`http://127.0.0.1:8080/admin/big-screen`

默认管理员账号（首次自动创建）：

- 用户名：`admin`
- 密码：`admin123`

## 关键接口

- 微信登录：`POST /api/auth/wechat-login`
- 训练历史：`GET /api/workouts/history`
- 月报：`GET /api/reports/monthly`
- 月报导出：`GET /api/reports/monthly/export`
- 通用上传：`POST /api/files/upload`
- 头像上传：`POST /api/files/avatar`
- 社区发帖：`POST /api/community/posts`
- 审核日志：`GET /api/admin/audit-logs`
- 社区审核：`PUT /api/admin/posts/{id}/audit`
- 大屏数据：`GET /api/admin/big-screen`

## 说明

- 文件上传后会生成 `/uploads/**` 访问路径，已在后端静态资源映射中开放。
- 社区帖子默认进入 `PENDING`，通过后台审核后对全体用户可见。
- 历史页导出会生成 CSV 到 `uploads/reports/`。
