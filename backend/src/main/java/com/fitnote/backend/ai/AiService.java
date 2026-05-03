package com.fitnote.backend.ai;

import com.fitnote.backend.exercise.ExerciseRepository;
import com.fitnote.backend.user.User;
import com.fitnote.backend.user.UserProfile;
import com.fitnote.backend.user.UserProfileRepository;
import com.fitnote.backend.user.UserRepository;
import com.fitnote.backend.workout.BodyMetricRepository;
import com.fitnote.backend.workout.PersonalRecord;
import com.fitnote.backend.workout.PersonalRecordRepository;
import com.fitnote.backend.workout.WorkoutSession;
import com.fitnote.backend.workout.WorkoutSessionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiService {

    private static final Logger log = LoggerFactory.getLogger(AiService.class);

    private static final String DEEPSEEK_URL = "https://api.deepseek.com/chat/completions";
    private static final String MODEL = "deepseek-chat";

    private static final String SYSTEM_PROMPT = """
        你是 Volt AI，FitNote 健身应用的智能AI助手。你的角色是专业健身教练兼运动科学顾问。

        你的专业领域：
        1. 训练计划制定与调整
        2. 动作技术指导与纠错
        3. 营养与饮食建议
        4. 恢复与休息策略
        5. 训练数据分析解读

        回答要求：
        - 用中文回复，专业但不生硬
        - 回复长度控制在 150-400 字
        - 如果用户问非健身问题，礼貌引导回健身话题
        - 给出的建议要有数据或原理支撑
        """;

    private final WorkoutSessionRepository workoutSessionRepository;
    private final BodyMetricRepository bodyMetricRepository;
    private final PersonalRecordRepository personalRecordRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final ExerciseRepository exerciseRepository;

    @Value("${app.ai.deepseek.api-key:}")
    private String apiKey;

    @Value("${app.ai.deepseek.enabled:false}")
    private boolean aiEnabled;

    public AiService(WorkoutSessionRepository workoutSessionRepository,
                     BodyMetricRepository bodyMetricRepository,
                     PersonalRecordRepository personalRecordRepository,
                     UserProfileRepository userProfileRepository,
                     UserRepository userRepository,
                     ExerciseRepository exerciseRepository) {
        this.workoutSessionRepository = workoutSessionRepository;
        this.bodyMetricRepository = bodyMetricRepository;
        this.personalRecordRepository = personalRecordRepository;
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public Map<String, Object> getPrompts() {
        return Map.of(
            "assistantName", "Volt AI",
            "status", "已就绪，帮助你突破极限",
            "cards", List.of(
                Map.of("title", "我今天该练什么？", "type", "training"),
                Map.of("title", "练后饮食建议", "type", "nutrition"),
                Map.of("title", "恢复评估报告", "type", "recovery"),
                Map.of("title", "近 30 天进展", "type", "trend")
            ),
            "welcomeMessage", "欢迎回来。我已根据你的最近训练与恢复数据，为你准备了今日训练建议。"
        );
    }

    public Map<String, Object> chat(Long userId, String message) {
        String userContext = buildUserContext(userId);
        String systemPromptWithContext = SYSTEM_PROMPT + "\n\n" + userContext;

        String reply;
        boolean usedAi;
        if (aiEnabled && apiKey != null && !apiKey.isBlank()) {
            reply = callDeepSeek(systemPromptWithContext, message);
            usedAi = true;
        } else {
            reply = localSmartReply(message, userId);
            usedAi = false;
        }

        return Map.of(
            "reply", reply,
            "model", usedAi ? MODEL : "local"
        );
    }

    private String buildUserContext(Long userId) {
        StringBuilder ctx = new StringBuilder();
        ctx.append("【当前用户训练数据】\n");

        userRepository.findById(userId).ifPresent(user ->
            ctx.append("- 昵称：").append(user.getNickname()).append("\n"));

        userProfileRepository.findByUserId(userId).ifPresent(profile -> {
            ctx.append("- 性别：").append(nullSafe(profile.getGender())).append("\n");
            ctx.append("- 身高：").append(profile.getHeightCm()).append("cm\n");
            ctx.append("- 体重：").append(profile.getWeightKg()).append("kg\n");
            ctx.append("- 目标：").append(nullSafe(profile.getTargetType())).append("\n");
            ctx.append("- 训练水平：").append(nullSafe(profile.getTrainingLevel())).append("\n");
        });

        List<WorkoutSession> recentSessions = workoutSessionRepository
            .findByUserIdOrderBySessionDateDesc(userId);
        if (!recentSessions.isEmpty()) {
            int count = Math.min(recentSessions.size(), 10);
            ctx.append("- 近").append(count).append("次训练：\n");
            for (int i = 0; i < count; i++) {
                WorkoutSession s = recentSessions.get(i);
                ctx.append("  ").append(s.getSessionDate()).append(" ")
                    .append(s.getTitle()).append("，训练量")
                    .append(s.getTotalVolume() != null ? s.getTotalVolume() : BigDecimal.ZERO)
                    .append("kg");
                if (s.getFeelingScore() != null) {
                    ctx.append("，感受评分").append(s.getFeelingScore()).append("/5");
                }
                ctx.append("\n");
            }
        }

        ctx.append("- 当前日期：").append(LocalDate.now()).append("\n");
        return ctx.toString();
    }

    private String callDeepSeek(String systemPrompt, String userMessage) {
        try {
            Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                    Map.of("role", "system", "content", systemPrompt),
                    Map.of("role", "user", "content", userMessage)
                ),
                "temperature", 0.7,
                "max_tokens", 800
            );

            RestClient client = RestClient.builder()
                .baseUrl(DEEPSEEK_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(new org.springframework.http.client.SimpleClientHttpRequestFactory() {{
                    setConnectTimeout(java.time.Duration.ofSeconds(5));
                    setReadTimeout(java.time.Duration.ofSeconds(30));
                }})
                .build();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = client.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(Map.class);

            if (response == null) return localSmartReply(userMessage, null);

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> msg = (Map<String, Object>) choices.get(0).get("message");
                if (msg != null && msg.get("content") != null) {
                    return msg.get("content").toString().trim();
                }
            }
            return localSmartReply(userMessage, null);
        } catch (Exception e) {
            log.error("DeepSeek API 调用失败：{}", e.getMessage());
            return localSmartReply(userMessage, null);
        }
    }

    private String localSmartReply(String message, Long userId) {
        String msg = message.toLowerCase().trim();

        if (msg.contains("练什么") || msg.contains("训练计划") || msg.contains("今天该练")) {
            return buildTrainingReply(userId);
        } else if (msg.contains("饮食") || msg.contains("吃什么") || msg.contains("营养")) {
            return buildNutritionReply(userId);
        } else if (msg.contains("恢复") || msg.contains("休息") || msg.contains("酸痛")) {
            return buildRecoveryReply(userId);
        } else if (msg.contains("进展") || msg.contains("进步") || msg.contains("趋势") || msg.contains("30天")) {
            return buildProgressReply(userId);
        } else if (msg.contains("动作") || msg.contains("怎么做") || msg.contains("标准") || msg.contains("要领")) {
            return buildTechniqueReply();
        } else if (msg.contains("减脂") || msg.contains("减肥") || msg.contains("瘦")) {
            return buildCutReply(userId);
        } else {
            return """
                收到你的问题！我是 Volt AI，你的专属健身教练助手。

                我可以帮你解答这些问题：
                🏋️ 训练计划制定和调整
                🍽 增肌/减脂营养策略
                🔄 恢复与休息优化
                📊 训练数据分析与进展评估
                📐 动作技术标准与纠错

                试试问我："我今天该练什么？"、"练后应该吃什么？"
                """;
        }
    }

    private String buildTrainingReply(Long userId) {
        UserProfile profile = getProfile(userId);
        String target = profile != null ? nullSafe(profile.getTargetType()) : "增肌";
        return """
            今天的训练建议基于你的%s目标：

            🔥 推荐：上背部 + 三角肌后束
            • 引体向上 4×8-12（拉满控制）
            • 杠铃划船 4×8-10
            • 高位下拉 3×10-12
            • 面拉 3×12-15
            • 哑铃侧平举 3×12-15

            如果状态不错，引体可以加负重尝试 5×5。
            """.formatted(target);
    }

    private String buildNutritionReply(Long userId) {
        UserProfile profile = getProfile(userId);
        String weight = profile != null && profile.getWeightKg() != null
            ? profile.getWeightKg().setScale(0, RoundingMode.HALF_UP).toString() : "70";
        return """
            基于你的训练数据（体重约%skg），饮食建议如下：

            📊 每日摄入参考
            • 蛋白质：每kg体重 1.8-2.0g
            • 碳水：蛋白质的 2-2.5 倍
            • 脂肪：总热量的 25%%

            🍽 练后窗口（训练后30-60分钟）
            快速吸收碳水+蛋白质，比如：两根香蕉 + 一勺乳清蛋白。

            如果体脂率持续上升过快，适当降低碳水 20%%，保持蛋白质不变。
            """.formatted(weight);
    }

    private String buildRecoveryReply(Long userId) {
        List<WorkoutSession> recent = getRecentSessions(userId, 7);
        int count = recent.size();
        String freq = count >= 3 ? "节奏合理" : "频率偏低，建议增加到每周3-4次";
        return """
            恢复评估报告 📋

            ✅ 训练频率：近7天 %d次，%s

            🔄 当前恢复策略建议：
            • 睡眠：确保每晚 7-8 小时
            • 主动恢复日：散步20-30分钟或轻量拉伸
            • 营养：练后补充蛋白质+碳水可加速糖原恢复

            ⚠️ 如果某个部位48小时后仍有明显酸痛，可能是训练量偏大。
            """.formatted(count, freq);
    }

    private String buildProgressReply(Long userId) {
        List<WorkoutSession> sessions = getRecentSessions(userId, 30);
        List<PersonalRecord> prs = personalRecordRepository.findTop10ByUserIdOrderByAchievedAtDesc(userId);

        int sessionCount = sessions.size();
        String prText = prs.isEmpty() ? "暂无PR数据" : prs.get(0).getExerciseName() + " " + prs.get(0).getRecordValue() + "kg";

        return """
            📈 近30天训练趋势分析

            🔹 训练次数：%d次
            🔹 最新PR：%s

            🎯 接下来建议：
            1. 继续渐进超负荷，每次尝试 +2.5kg 或 +1rep
            2. 每4-6周安排一次减载周
            3. 关注深蹲和硬拉的PR窗口
            """.formatted(sessionCount, prText);
    }

    private String buildTechniqueReply() {
        return """
            📐 动作标准参考

            以杠铃卧推为例，关键要领：

            1. 起桥：肩胛骨收紧下沉，胸椎轻微起桥，臀部不离凳
            2. 握距：约 1.5 倍肩宽，手腕中立不后翻
            3. 下放：控制 2-3 秒，杠铃触胸中下部
            4. 推起：爆发推起，不要弹胸借力
            5. 呼吸：下放吸气，推起时憋气维持腹压

            ⚠️ 常见错误：
            • 肩膀前送 → 肩关节压力大
            • 臀部离凳 → 腰椎风险
            • 半程卧推 → 胸肌刺激不足
            """;
    }

    private String buildCutReply(Long userId) {
        UserProfile profile = getProfile(userId);
        String weight = profile != null && profile.getWeightKg() != null
            ? profile.getWeightKg().toString() : "75";
        return """
            减脂期训练策略 🏃

            核心原则：保持力量训练 + 增加热量缺口

            📋 建议安排：
            • 每周 4 天力量训练（保持训练强度，避免肌肉流失）
            • 每周 2-3 次有氧（空腹或力量训练后，20-30分钟）
            • 每日热量缺口：300-500 kcal
            • 蛋白质摄入不低于 2g/kg 体重（约%sg/天）

            ⚠️ 一旦力量明显下降，立即减少热量缺口或增加碳水。
            """.formatted((int)(Double.parseDouble(weight) * 2));
    }

    private UserProfile getProfile(Long userId) {
        if (userId == null) return null;
        return userProfileRepository.findByUserId(userId).orElse(null);
    }

    private List<WorkoutSession> getRecentSessions(Long userId, int days) {
        if (userId == null) return List.of();
        return workoutSessionRepository.findByUserIdOrderBySessionDateDesc(userId).stream()
            .filter(s -> !s.getSessionDate().isBefore(LocalDate.now().minusDays(days)))
            .toList();
    }

    private static String nullSafe(String value) {
        return value == null || value.isBlank() ? "未设置" : value;
    }
}
