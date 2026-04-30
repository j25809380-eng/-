# AGENTS.md

## Project overview

FitNote — WeChat miniprogram fitness tracker for a graduation project. Backend is Spring Boot 3 + JPA + MySQL, frontend is native WeChat miniprogram. No Node/JS build toolchain exists; the miniprogram is opened directly in WeChat DevTools.

```
backend/     → Spring Boot (Maven), package: com.fitnote.backend, Java 21, port 8080
miniprogram/ → WeChat miniprogram (native, no bundler)
docs/        → Design & defense documents
_1/ … _9/   → AI-generated screenshots + HTML — do not touch
```

## Commands

All commands run from repo root unless noted.

```powershell
# Start MySQL + Adminer (Docker Desktop required)
docker compose up -d

# Start backend (default: mysql profile, port 8080)
$env:JAVA_HOME="C:\Program Files\Java\jdk-21.0.10"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn spring-boot:run

# Start backend on different port
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"

# Start in demo mode (H2 + seed data, no MySQL)
mvn spring-boot:run "-Dspring-boot.run.profiles=demo"

# WeChat login requires env vars (only for real WeChat auth)
$env:WECHAT_APP_ID="wx..."
$env:WECHAT_APP_SECRET="..."

# There are no tests in this repo. There is no lint/format/typecheck.
# Enable real AI chat (requires DeepSeek API key):
$env:DEEPSEEK_API_KEY="sk-..."
# Then set app.ai.deepseek.enabled: true in application.yml
```

**Maven wrapper**: There is no `mvnw`. Use your system `mvn` (Maven 3.8+).

## Architecture

### Backend package layout
```
com.fitnote.backend
  ├─ auth/         → WeChat login, JWT
  ├─ user/         → User + UserProfile CRUD
  ├─ exercise/     → Exercise catalog
  ├─ plan/         → TrainingPlan, TrainingPlanDay, TrainingPlanItem
  ├─ workout/      → WorkoutSession, WorkoutSet, BodyMetric, PersonalRecord
  ├─ community/    → CommunityPost, CommunityComment, CommunityLike
  ├─ analytics/    → Dashboard + Analytics endpoints
  ├─ reports/      → Monthly CSV export
  ├─ admin/        → Admin controllers, audit logs, big-screen data
  ├─ security/     → SecurityConfig, JwtAuthenticationFilter, JwtTokenProvider
  ├─ common/       → ApiResponse, BaseEntity, GlobalExceptionHandler
  ├─ config/       → WebResourceConfig, AiController (DeepSeek proxy + local fallback)
  └─ bootstrap/    → AdminInitializer, DemoDataInitializer
```

### Miniprogram request layer (`miniprogram/utils/request.js`)
- Tries `baseUrls` in order (config.js: 8080 then 8081), remembers the first working one
- `useMock: false` and `fallbackToMock: false` — mock layer exists but is disabled
- File URLs returned by the API are relative (`/uploads/...`); use `resolveFileUrl()` which strips `/api` from the base URL to construct the origin

### Auth
- JWT token stored in `app.globalData.token` and `wx.getStorageSync('fitnote_token')`
- Sent as `Authorization: Bearer <token>`
- Admin uses separate login (`/api/admin/auth/login`) with default credentials `admin` / `admin123`

### Profiles
- `mysql` — active by default, uses MySQL on localhost:3306, `jpa.hibernate.ddl-auto: update` (auto-schema)
- `demo` — H2 in-memory, seeds demo data via `DemoDataInitializer`

## Key quirks

- **No tests exist.** Don't run test commands — they will produce no results.
- **AI chat** (`/api/ai/chat`) uses DeepSeek when `app.ai.deepseek.enabled=true` and `DEEPSEEK_API_KEY` is set. Otherwise falls back to local smart-reply (6 predefined scenarios). No extra dependencies needed — uses Spring `RestClient`.
- **SQL schema** is at `backend/src/main/resources/sql/schema-mysql.sql`. If MySQL was started via docker-compose, it auto-runs on first init. Otherwise run manually.
- **Upload directory**: `backend/uploads/` is served as static resources via `spring.web.resources.static-locations`. Files return relative paths.
- **Admin panel** is plain HTML/JS/CSS at `backend/src/main/resources/static/admin/`, served by Spring Boot static resource mapping (no build step).
- **miniprogram has no npm/package.json** — everything is native WeChat APIs.
- **project.config.json** specifies `"miniprogramRoot": "miniprogram/"` — WeChat DevTools should open the repo root.
- **JPA `ddl-auto: update`** means tables are auto-created/updated on startup. No Flyway/Liquibase migrations.
- **API response envelope**: all responses use `ApiResponse` with structure `{ code: 0, data: ... }`. `code !== 0` means error.
