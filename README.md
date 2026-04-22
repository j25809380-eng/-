# FitNote

FitNote 是一个面向毕业设计答辩的健身记录类微信小程序项目，基于你现有的 Stitch 高保真 UI 原型继续开发，目标是做到界面高级、功能完整、结构清晰、可直接运行与二次扩展。

## 技术选型

- 小程序前端：微信小程序原生
- 后端服务：Spring Boot 3
- 安全认证：JWT
- 数据库：MySQL 8（同时提供 `demo` 运行模式，方便先行演示）
- 数据接口风格：RESTful API

## 仓库结构

```text
FitNote/
├─ backend/                  Spring Boot 后端
├─ docs/                     架构、数据库、接口映射文档
├─ miniprogram/              微信小程序原生工程
├─ _1 ~ _9 / ai/             Stitch 导出的原始页面原型
└─ README.md
```

## 第一阶段已完成

- 基于 Stitch 页面完成业务模块拆解
- 输出项目整体架构设计文档
- 输出数据库设计文档与 MySQL 初始化脚本
- 输出小程序页面与接口映射表
- 搭建微信小程序原生工程骨架
- 搭建 Spring Boot 后端骨架
- 完成用户、训练计划、动作库、训练记录、社区、AI 助手、个人中心等第一阶段页面与接口
- 支持 `demo` 模式直接运行，方便先联调和答辩演示

## 第二阶段已完成

- 小程序默认切换为真实接口优先，支持多端口自动尝试和 mock 回退
- 新增 `personal_record` 持久化能力
- 新增自定义训练计划接口
- 增强统计接口，补充训练量趋势与 PR 看板
- 增加后台管理系统第一版
- 增加后台数据总览、用户管理、训练数据、社区审核、计划管理接口

## 第三阶段已完成

- 新增自定义训练计划创建页，支持模板套用、训练日配置、动作增删与保存
- 社区模块接入点赞、评论、评论预览与关注/热门切换逻辑
- 历史页接入月度训练报告接口，展示周频分布与主力动作训练量
- 后台管理端增加管理员登录、登录态保持、退出登录与接口权限控制
- 增加管理员种子账号、社区评论点赞演示数据与统一异常响应

## 第四阶段已完成

- 小程序个人中心接入头像上传与服务端资源回显
- 后台新增数据大屏入口 `/admin/big-screen`
- 新增大屏接口 `GET /api/admin/big-screen`
- 增加 Docker Compose 一键启动 MySQL 与 Adminer
- 增加第四阶段说明、答辩提纲与项目演示脚本文档

## 最终收官补充

- 微信登录改为“mock / 真实微信登录”可切换的工程化实现
- 新增通用文件上传接口，支持社区图片动态与后续扩展
- 社区页支持图片动态发布
- 后台新增审核日志接口与日志页面
- 补齐 `/uploads/**` 静态资源映射，确保头像与社区图片可直接访问

## 小程序运行

1. 打开微信开发者工具。
2. 选择导入项目，项目目录指向仓库根目录。
3. 将 `miniprogramRoot` 设为 `miniprogram`。
4. 首次运行建议勾选“不校验合法域名、web-view（业务域名）、TLS 版本以及 HTTPS 证书”。
5. 项目默认真实接口优先：
   - 若本地后端已启动，会优先请求真实后端
   - 若后端未启动，会自动回退 mock 数据

## 后端运行

### 方式一：直接运行演示模式

1. 进入 `backend` 目录。
2. 执行：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run
```

3. 默认启用 `demo` 配置，接口地址为 `http://localhost:8080/api`。

### 方式二：如果本机 8080 被占用

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

小程序请求层会自动尝试：

- `http://127.0.0.1:8080/api`
- `http://127.0.0.1:8081/api`

### 方式三：切换到 MySQL 模式

1. 先创建数据库 `fitnote`。
2. 执行 `backend/src/main/resources/sql/schema-mysql.sql` 初始化表结构。
3. 修改 `backend/src/main/resources/application-mysql.yml` 中的数据库账号密码。
4. 运行：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
```

### 方式四：Docker 一键启动 MySQL

在仓库根目录执行：

```powershell
docker compose up -d
```

然后再启动后端的 `mysql` 配置即可。

## 后台管理端

启动后端后可直接访问：

- `http://127.0.0.1:8080/admin`
- 或 `http://127.0.0.1:8081/admin`

数据大屏：

- `http://127.0.0.1:8080/admin/big-screen`
- 或 `http://127.0.0.1:8081/admin/big-screen`

审核日志：

- 在后台管理端导航中直接进入“审核日志”

演示账号：

- 管理员账号：`admin`
- 管理员密码：`admin123`

## 文档入口

- [整体架构设计](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\docs\architecture.md)
- [数据库设计](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\docs\database-design.md)
- [页面与接口映射](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\docs\page-api-mapping.md)
- [项目目录结构](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\docs\project-structure.md)
- [第二阶段说明](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\docs\phase-two.md)
- [第三阶段说明](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\docs\phase-three.md)
- [第四阶段说明](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\docs\phase-four.md)
- [答辩提纲](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\docs\defense-outline.md)
- [演示脚本](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\docs\demo-script.md)
- [最终交付说明](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\docs\final-delivery.md)

## 下一阶段建议

- 完成真实微信登录 `code2Session`
- 将演示数据全部替换为 MySQL 持久化
- 接入图表组件与月报导出
- 增加文件上传、头像更新与社区图片发布
- 增加后台大屏与更完整的审核日志追踪
