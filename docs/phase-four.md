# FitNote 第四阶段说明

第四阶段的重点，是把项目从“功能完成”推进到“更像真实商业项目、适合毕业答辩展示”的层次。

## 本阶段完成内容

### 1. 小程序用户资料页增强

- 支持头像选择与上传
- 支持本地预览和服务端资源地址解析
- 上传后同步更新用户资料状态

### 2. 后台数据大屏

- 新增 `/admin/big-screen` 数据大屏入口
- 新增 `GET /api/admin/big-screen` 数据接口
- 展示核心指标、训练趋势、训练量趋势、用户目标分布、训练等级分布、热门动作、活跃用户榜、审核预警和热力条

### 3. MySQL 一键演示支持

- 根目录新增 `docker-compose.yml`
- 可一键启动 MySQL 8 和 Adminer
- MySQL 初始化自动执行 `schema-mysql.sql`

### 4. 答辩配套材料

- 增加答辩提纲文档
- 增加项目演示脚本说明

## 本阶段建议演示路径

1. 打开小程序个人中心，上传头像并保存资料。
2. 展示训练计划、自定义计划、社区互动、月度报告。
3. 打开后台管理系统并登录管理员账号。
4. 从后台切换到数据大屏展示整体项目价值。
5. 结合答辩提纲说明系统架构、功能设计和创新点。

## MySQL 运行方式

在仓库根目录执行：

```powershell
docker compose up -d
```

启动后：

- MySQL：`127.0.0.1:3306`
- 数据库：`fitnote`
- 用户名：`root`
- 密码：`123456`
- Adminer：`http://127.0.0.1:8088`

然后在 `backend` 目录运行：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run "-Dspring-boot.run.profiles=mysql"
```
