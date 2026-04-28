# FitNote 数据库设计

## 1. 数据库概览

数据库名称：`fitnote`

字符集建议：

- `utf8mb4`
- `utf8mb4_unicode_ci`

数据库模块分为：

- 用户域
- 动作与计划域
- 训练记录域
- 数据分析域
- 社区域
- 后台管理域

## 2. 核心表说明

### 2.1 用户域

#### `app_user`

存储小程序用户基础账号信息。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 用户主键 |
| open_id | varchar(64) | 微信 openId |
| union_id | varchar(64) | 微信 unionId |
| nickname | varchar(64) | 昵称 |
| avatar_url | varchar(255) | 头像地址 |
| phone | varchar(20) | 手机号 |
| status | tinyint | 状态，1 正常 0 禁用 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

#### `user_profile`

存储用户身体信息与目标。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| user_id | bigint | 用户 ID |
| gender | varchar(16) | 性别 |
| height_cm | decimal(5,2) | 身高 |
| weight_kg | decimal(5,2) | 体重 |
| body_fat_rate | decimal(5,2) | 体脂率 |
| target_type | varchar(32) | 目标类型 |
| target_weight_kg | decimal(5,2) | 目标体重 |
| training_level | varchar(32) | 训练等级 |
| bio | varchar(255) | 简介 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

#### `user_body_metric`

用于记录体重和身体围度趋势。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| user_id | bigint | 用户 ID |
| metric_date | date | 记录日期 |
| weight_kg | decimal(5,2) | 体重 |
| body_fat_rate | decimal(5,2) | 体脂 |
| skeletal_muscle_kg | decimal(5,2) | 骨骼肌 |
| remark | varchar(255) | 备注 |
| created_at | datetime | 创建时间 |

### 2.2 动作与计划域

#### `exercise`

存储动作库基础信息。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| name | varchar(64) | 动作名称 |
| category | varchar(32) | 部位分类 |
| equipment | varchar(32) | 器械类型 |
| difficulty | varchar(16) | 难度 |
| primary_muscle | varchar(32) | 主肌群 |
| secondary_muscles | varchar(255) | 次要肌群 |
| cover_image | varchar(255) | 封面图 |
| description | text | 动作介绍 |
| movement_steps | text | JSON 步骤 |
| tips | text | JSON 要点 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

#### `training_plan`

存储训练计划主表。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| title | varchar(64) | 标题 |
| subtitle | varchar(128) | 副标题 |
| target_type | varchar(32) | 目标类型 |
| difficulty | varchar(16) | 难度 |
| duration_weeks | int | 周期周数 |
| days_per_week | int | 每周训练天数 |
| summary | varchar(255) | 简介 |
| cover_image | varchar(255) | 封面 |
| is_custom | tinyint | 是否自定义 |
| creator_user_id | bigint | 创建者 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

#### `training_plan_day`

存储计划的每日安排。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| plan_id | bigint | 计划 ID |
| day_no | int | 第几天 |
| title | varchar(64) | 标题 |
| focus | varchar(64) | 训练重点 |
| created_at | datetime | 创建时间 |

#### `training_plan_item`

存储每日计划中的动作项。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| day_id | bigint | 日计划 ID |
| exercise_id | bigint | 动作 ID |
| exercise_name | varchar(64) | 动作名称快照 |
| sets_count | int | 组数 |
| reps | varchar(32) | 次数区间 |
| rest_seconds | int | 休息秒数 |
| weight_mode | varchar(32) | 重量说明 |
| sort_no | int | 排序 |

### 2.3 训练记录域

#### `workout_session`

存储一次完整训练。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| user_id | bigint | 用户 ID |
| plan_id | bigint | 关联计划 |
| title | varchar(64) | 训练标题 |
| focus | varchar(64) | 训练重点 |
| session_date | date | 训练日期 |
| started_at | datetime | 开始时间 |
| finished_at | datetime | 结束时间 |
| duration_minutes | int | 时长 |
| total_volume | decimal(10,2) | 总训练量 |
| calories | int | 消耗热量 |
| feeling_score | int | 主观感受 |
| notes | varchar(255) | 备注 |
| completion_status | varchar(16) | 状态 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

#### `workout_set`

存储训练组明细。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| session_id | bigint | 训练 ID |
| exercise_id | bigint | 动作 ID |
| exercise_name | varchar(64) | 动作名称快照 |
| set_no | int | 第几组 |
| weight_kg | decimal(8,2) | 重量 |
| reps | int | 次数 |
| rir | int | 预留次数 |
| remark | varchar(128) | 备注 |
| is_pr | tinyint | 是否 PR |
| created_at | datetime | 创建时间 |

#### `personal_record`

存储个人记录历史。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| user_id | bigint | 用户 ID |
| exercise_id | bigint | 动作 ID |
| exercise_name | varchar(64) | 动作名称 |
| record_type | varchar(32) | 1RM / volume / reps |
| record_value | decimal(10,2) | 记录值 |
| achieved_at | datetime | 达成时间 |
| source_session_id | bigint | 来源训练 |

### 2.4 社区域

#### `community_post`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| user_id | bigint | 用户 ID |
| content | text | 动态正文 |
| cover_image | varchar(255) | 封面图 |
| post_type | varchar(32) | 动态类型 |
| topic_tags | varchar(255) | 话题标签 |
| like_count | int | 点赞数 |
| comment_count | int | 评论数 |
| collect_count | int | 收藏数 |
| audit_status | varchar(16) | 审核状态 |
| created_at | datetime | 创建时间 |
| updated_at | datetime | 更新时间 |

#### `community_comment`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| post_id | bigint | 动态 ID |
| user_id | bigint | 评论用户 |
| parent_id | bigint | 父评论 |
| content | varchar(500) | 评论内容 |
| created_at | datetime | 创建时间 |

#### `community_like`

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | bigint PK | 主键 |
| post_id | bigint | 动态 ID |
| user_id | bigint | 点赞用户 |
| created_at | datetime | 创建时间 |

### 2.5 后台与审核域

#### `sys_admin`

后台管理员账号。

#### `content_audit_log`

社区内容审核日志。

#### `data_report_monthly`

月报统计结果缓存表。

## 3. 关系说明

- `app_user` 1 对 1 `user_profile`
- `app_user` 1 对多 `user_body_metric`
- `training_plan` 1 对多 `training_plan_day`
- `training_plan_day` 1 对多 `training_plan_item`
- `workout_session` 1 对多 `workout_set`
- `app_user` 1 对多 `workout_session`
- `app_user` 1 对多 `community_post`
- `community_post` 1 对多 `community_comment`

## 4. 索引建议

- `app_user.open_id` 唯一索引
- `user_body_metric(user_id, metric_date)` 组合索引
- `exercise(category, primary_muscle)` 组合索引
- `training_plan(target_type, difficulty)` 索引
- `workout_session(user_id, session_date)` 组合索引
- `workout_set(session_id, exercise_id)` 组合索引
- `personal_record(user_id, exercise_id, record_type)` 组合索引
- `community_post(created_at)` 索引
- `community_post(audit_status, created_at)` 组合索引

## 5. SQL 文件位置

完整初始化 SQL 位于：

- [schema-mysql.sql](C:\Users\25809\Desktop\stitch_onyx_fitness_tracker\backend\src\main\resources\sql\schema-mysql.sql)
