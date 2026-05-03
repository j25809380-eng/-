CREATE DATABASE IF NOT EXISTS fitnote DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fitnote;

CREATE TABLE IF NOT EXISTS app_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    open_id VARCHAR(64) NOT NULL UNIQUE,
    union_id VARCHAR(64),
    nickname VARCHAR(64) NOT NULL,
    avatar_url VARCHAR(255),
    phone VARCHAR(20),
    status TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    gender VARCHAR(16),
    height_cm DECIMAL(5,2),
    weight_kg DECIMAL(5,2),
    body_fat_rate DECIMAL(5,2),
    target_type VARCHAR(32),
    target_weight_kg DECIMAL(5,2),
    training_level VARCHAR(32),
    bio VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

CREATE TABLE IF NOT EXISTS user_body_metric (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    metric_date DATE NOT NULL,
    weight_kg DECIMAL(5,2),
    body_fat_rate DECIMAL(5,2),
    skeletal_muscle_kg DECIMAL(5,2),
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_metric_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    INDEX idx_metric_user_date (user_id, metric_date)
);

CREATE TABLE IF NOT EXISTS exercise (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    category VARCHAR(32),
    equipment VARCHAR(32),
    difficulty VARCHAR(16),
    primary_muscle VARCHAR(32),
    secondary_muscles VARCHAR(255),
    cover_image VARCHAR(255),
    description TEXT,
    movement_steps TEXT,
    tips TEXT,
    is_compound TINYINT DEFAULT 0,
    priority INT DEFAULT 5,
    suitable_level VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_exercise_category (category, primary_muscle)
);

CREATE TABLE IF NOT EXISTS training_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    title VARCHAR(64) NOT NULL,
    subtitle VARCHAR(128),
    target_type VARCHAR(32),
    difficulty VARCHAR(16),
    duration_weeks INT,
    days_per_week INT,
    summary VARCHAR(255),
    cover_image VARCHAR(255),
    is_custom TINYINT DEFAULT 0,
    creator_user_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plan_target (target_type, difficulty)
);

CREATE TABLE IF NOT EXISTS training_plan_day (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plan_id BIGINT NOT NULL,
    day_no INT NOT NULL,
    title VARCHAR(64) NOT NULL,
    focus VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_day_plan FOREIGN KEY (plan_id) REFERENCES training_plan(id)
);

CREATE TABLE IF NOT EXISTS training_plan_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    day_id BIGINT NOT NULL,
    exercise_id BIGINT,
    exercise_name VARCHAR(64),
    sets_count INT,
    reps VARCHAR(32),
    rest_seconds INT,
    weight_mode VARCHAR(32),
    sort_no INT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan_item_day FOREIGN KEY (day_id) REFERENCES training_plan_day(id)
);

CREATE TABLE IF NOT EXISTS workout_session (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    plan_id BIGINT,
    title VARCHAR(64) NOT NULL,
    focus VARCHAR(64),
    session_date DATE NOT NULL,
    started_at DATETIME,
    finished_at DATETIME,
    duration_minutes INT,
    total_volume DECIMAL(10,2),
    calories INT,
    feeling_score INT,
    notes VARCHAR(255),
    completion_status VARCHAR(16),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_workout_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    INDEX idx_workout_user_date (user_id, session_date)
);

CREATE TABLE IF NOT EXISTS workout_set (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    session_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT,
    exercise_name VARCHAR(64),
    set_no INT,
    weight_kg DECIMAL(8,2),
    reps INT,
    rir INT,
    remark VARCHAR(128),
    is_pr TINYINT DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_set_session FOREIGN KEY (session_id) REFERENCES workout_session(id),
    INDEX idx_set_session_exercise (session_id, exercise_id)
);

CREATE TABLE IF NOT EXISTS personal_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    exercise_id BIGINT NOT NULL,
    exercise_name VARCHAR(64),
    record_type VARCHAR(32),
    record_value DECIMAL(10,2),
    achieved_at DATETIME,
    source_session_id BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_pr_user_exercise (user_id, exercise_id, record_type)
);

CREATE TABLE IF NOT EXISTS community_post (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    author_name VARCHAR(64),
    author_avatar VARCHAR(255),
    content TEXT,
    cover_image VARCHAR(255),
    post_type VARCHAR(32),
    topic_tags VARCHAR(255),
    like_count INT DEFAULT 0,
    comment_count INT DEFAULT 0,
    collect_count INT DEFAULT 0,
    audit_status VARCHAR(16) DEFAULT 'APPROVED',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    INDEX idx_post_created (created_at),
    INDEX idx_post_audit_created (audit_status, created_at)
);

CREATE TABLE IF NOT EXISTS community_comment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    parent_id BIGINT,
    content VARCHAR(500) NOT NULL,
    author_name VARCHAR(64),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES community_post(id),
    INDEX idx_comment_post_created (post_id, created_at)
);

CREATE TABLE IF NOT EXISTS community_like (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES community_post(id),
    UNIQUE KEY uk_like_post_user (post_id, user_id)
);

CREATE TABLE IF NOT EXISTS sys_admin (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    nickname VARCHAR(64),
    role_code VARCHAR(32),
    status TINYINT DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS food_preset (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    category VARCHAR(32),
    meal_type VARCHAR(16),
    kcal INT NOT NULL,
    protein DECIMAL(6,1),
    carbs DECIMAL(6,1),
    fat DECIMAL(6,1)
);

CREATE TABLE IF NOT EXISTS content_audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_type VARCHAR(32),
    target_id BIGINT,
    previous_status VARCHAR(16),
    audit_status VARCHAR(16),
    reason VARCHAR(255),
    operator_id BIGINT,
    operator_name VARCHAR(64),
    target_snapshot VARCHAR(1000),
    extra_payload VARCHAR(1000),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_audit_log_created (created_at),
    INDEX idx_audit_log_target (target_type, target_id),
    INDEX idx_audit_log_status (audit_status, created_at),
    INDEX idx_audit_log_operator (operator_id, created_at)
);

CREATE TABLE IF NOT EXISTS user_follow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_follow (follower_id, following_id),
    INDEX idx_follow_follower (follower_id),
    INDEX idx_follow_following (following_id)
);

CREATE TABLE IF NOT EXISTS data_report_monthly (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    report_month VARCHAR(16) NOT NULL,
    sessions_count INT DEFAULT 0,
    total_volume DECIMAL(12,2) DEFAULT 0,
    pr_count INT DEFAULT 0,
    weight_change DECIMAL(6,2) DEFAULT 0,
    summary TEXT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_report_user_month (user_id, report_month)
);

-- ========== 饮食分析系统 ==========

CREATE TABLE IF NOT EXISTS user_goal (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    goal_type VARCHAR(16) NOT NULL DEFAULT 'maintain',
    target_kcal INT NOT NULL DEFAULT 2200,
    target_protein INT NOT NULL DEFAULT 120,
    target_carbs INT NOT NULL DEFAULT 275,
    target_fat INT NOT NULL DEFAULT 60,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_goal_user FOREIGN KEY (user_id) REFERENCES app_user(id)
);

CREATE TABLE IF NOT EXISTS diet_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    log_date DATE NOT NULL,
    name VARCHAR(64) NOT NULL,
    meal_type VARCHAR(16),
    kcal INT NOT NULL DEFAULT 0,
    protein DECIMAL(6,1) NOT NULL DEFAULT 0,
    carbs DECIMAL(6,1) NOT NULL DEFAULT 0,
    fat DECIMAL(6,1) NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_dietlog_user FOREIGN KEY (user_id) REFERENCES app_user(id),
    INDEX idx_dietlog_user_date (user_id, log_date)
);
