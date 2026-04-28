# FitNote 项目目录结构

```text
FitNote
├─ miniprogram
│  ├─ app.js
│  ├─ app.json
│  ├─ app.wxss
│  ├─ sitemap.json
│  ├─ styles
│  │  └─ theme.wxss
│  ├─ utils
│  │  ├─ config.js
│  │  ├─ mock.js
│  │  └─ request.js
│  ├─ components
│  │  ├─ bottom-nav
│  │  └─ section-title
│  └─ pages
│     ├─ login
│     ├─ home
│     ├─ plan
│     ├─ exercise
│     ├─ exercise-detail
│     ├─ history
│     ├─ workout-editor
│     ├─ workout-complete
│     ├─ ai
│     ├─ community
│     ├─ ranking
│     └─ profile
├─ backend
│  ├─ pom.xml
│  └─ src/main
│     ├─ java/com/fitnote/backend
│     │  ├─ FitNoteApplication.java
│     │  ├─ common
│     │  ├─ security
│     │  ├─ auth
│     │  ├─ user
│     │  ├─ exercise
│     │  ├─ plan
│     │  ├─ workout
│     │  ├─ analytics
│     │  ├─ community
│     │  ├─ admin
│     │  ├─ bootstrap
│     │  └─ config
│     └─ resources
│        ├─ application.yml
│        ├─ application-demo.yml
│        ├─ application-mysql.yml
│        ├─ static/admin
│        └─ sql/schema-mysql.sql
├─ docs
│  ├─ architecture.md
│  ├─ database-design.md
│  ├─ page-api-mapping.md
│  ├─ project-structure.md
│  └─ phase-two.md
├─ _1 ~ _9 / ai
│  ├─ code.html
│  └─ screen.png
├─ project.config.json
├─ .gitignore
└─ README.md
```

## 分层说明

- `miniprogram/pages`：实际业务页面，对应 Stitch 原型和新增业务页
- `miniprogram/components`：通用 UI 组件
- `miniprogram/utils`：请求封装、环境配置、mock 数据
- `backend/common`：统一响应与公共实体
- `backend/security`：JWT 鉴权与安全配置
- `backend/bootstrap`：演示数据初始化
- `backend/resources/static/admin`：后台管理系统静态页面
- `backend/*` 业务模块：按用户、训练、计划、社区、分析、管理端拆分
