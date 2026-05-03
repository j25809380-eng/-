package com.fitnote.backend.nutrition;

import com.fitnote.backend.common.PageResult;
import com.fitnote.backend.user.UserProfile;
import com.fitnote.backend.user.UserProfileRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NutritionService implements INutritionService {

    private final UserGoalRepository userGoalRepository;
    private final DietLogRepository dietLogRepository;
    private final UserProfileRepository userProfileRepository;
    private final FoodPresetRepository foodPresetRepository;

    public NutritionService(UserGoalRepository userGoalRepository,
                            DietLogRepository dietLogRepository,
                            UserProfileRepository userProfileRepository,
                            FoodPresetRepository foodPresetRepository) {
        this.userGoalRepository = userGoalRepository;
        this.dietLogRepository = dietLogRepository;
        this.userProfileRepository = userProfileRepository;
        this.foodPresetRepository = foodPresetRepository;
    }

    // ========== 目标管理 ==========

    public UserGoal getGoal(Long userId) {
        return userGoalRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultGoal(userId));
    }

    public UserGoal updateGoal(Long userId, String goalType) {
        UserGoal goal = userGoalRepository.findByUserId(userId)
            .orElseGet(() -> createDefaultGoal(userId));
        goal.setGoalType(goalType);

        UserProfile profile = userProfileRepository.findByUserId(userId).orElse(null);
        BigDecimal weightKg = profile != null && profile.getWeightKg() != null
            ? profile.getWeightKg() : new BigDecimal("70");

        int bmr = BigDecimal.valueOf(370)
            .add(BigDecimal.valueOf(21.6)
                .multiply(weightKg.subtract(weightKg.multiply(profile != null && profile.getBodyFatRate() != null
                    ? profile.getBodyFatRate().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : new BigDecimal("0.20")))))
            .intValue();

        int tdee = (int)(bmr * 1.55);

        int targetKcal;
        double proteinFactor;
        switch (goalType) {
            case "cut":
                targetKcal = tdee - 400;
                proteinFactor = 2.0;
                break;
            case "bulk":
                targetKcal = tdee + 350;
                proteinFactor = 2.0;
                break;
            default:
                targetKcal = tdee;
                proteinFactor = 1.8;
                break;
        }

        int targetProtein = (int)(weightKg.doubleValue() * proteinFactor);
        int proteinKcal = targetProtein * 4;
        int remainingKcal = targetKcal - proteinKcal;
        int targetFat = remainingKcal * 30 / 100 / 9;
        int targetCarbs = (remainingKcal - targetFat * 9) / 4;

        goal.setTargetKcal(targetKcal);
        goal.setTargetProtein(targetProtein);
        goal.setTargetCarbs(targetCarbs);
        goal.setTargetFat(targetFat);
        return userGoalRepository.save(goal);
    }

    private UserGoal createDefaultGoal(Long userId) {
        UserGoal goal = new UserGoal();
        goal.setUserId(userId);
        goal.setGoalType("maintain");
        goal.setTargetKcal(2200);
        goal.setTargetProtein(120);
        goal.setTargetCarbs(275);
        goal.setTargetFat(60);
        return userGoalRepository.save(goal);
    }

    // ========== 饮食记录 ==========

    @Transactional
    public DietLog addLog(Long userId, String name, String mealType,
                          Integer kcal, BigDecimal protein,
                          BigDecimal carbs, BigDecimal fat) {
        DietLog log = new DietLog();
        log.setUserId(userId);
        log.setLogDate(LocalDate.now());
        log.setName(name);
        log.setMealType(mealType == null ? "other" : mealType);
        log.setKcal(kcal);
        log.setProtein(protein);
        log.setCarbs(carbs);
        log.setFat(fat);
        return dietLogRepository.save(log);
    }

    public List<DietLog> getTodayLogs(Long userId) {
        return dietLogRepository.findByUserIdAndLogDate(userId, LocalDate.now());
    }

    public PageResult<Map<String, Object>> getLogs(Long userId, LocalDate logDate, int page, int size) {
        List<Map<String, Object>> all = dietLogRepository.findByUserIdAndLogDate(userId, logDate).stream()
            .map(log -> Map.<String, Object>of(
                "id", log.getId(),
                "name", log.getName(),
                "mealType", log.getMealType(),
                "kcal", log.getKcal(),
                "protein", log.getProtein(),
                "carbs", log.getCarbs(),
                "fat", log.getFat(),
                "logDate", log.getLogDate().toString()
            ))
            .toList();
        return PageResult.fromList(all, page, size);
    }

    @Transactional
    public void deleteLog(Long id) {
        dietLogRepository.deleteById(id);
    }

    // ========== 食物预设库 ==========

    public List<Map<String, Object>> getFoodPresets(String keyword) {
        List<FoodPreset> presets = (keyword == null || keyword.isBlank())
            ? foodPresetRepository.findAll()
            : foodPresetRepository.findByNameContainingIgnoreCase(keyword);
        return presets.stream()
            .map(p -> Map.<String, Object>of(
                "id", p.getId(),
                "name", p.getName(),
                "category", p.getCategory() != null ? p.getCategory() : "",
                "mealType", p.getMealType() != null ? p.getMealType() : "other",
                "kcal", p.getKcal(),
                "protein", p.getProtein(),
                "carbs", p.getCarbs(),
                "fat", p.getFat()
            ))
            .toList();
    }

    // ========== 每日分析 ==========

    public Map<String, Object> getTodayAnalysis(Long userId) {
        List<DietLog> logs = getTodayLogs(userId);
        UserGoal goal = getGoal(userId);

        int totalKcal = 0;
        double totalProtein = 0, totalCarbs = 0, totalFat = 0;
        for (DietLog log : logs) {
            totalKcal += log.getKcal();
            totalProtein += log.getProtein().doubleValue();
            totalCarbs += log.getCarbs().doubleValue();
            totalFat += log.getFat().doubleValue();
        }

        double proteinPct = goal.getTargetProtein() > 0 ? totalProtein / goal.getTargetProtein() * 100 : 0;
        double carbsPct = goal.getTargetCarbs() > 0 ? totalCarbs / goal.getTargetCarbs() * 100 : 0;
        double fatPct = goal.getTargetFat() > 0 ? totalFat / goal.getTargetFat() * 100 : 0;
        double kcalPct = goal.getTargetKcal() > 0 ? (double)totalKcal / goal.getTargetKcal() * 100 : 0;

        int score = calculateScore(goal.getGoalType(), totalKcal, totalProtein, totalCarbs, totalFat, goal);
        String grade = gradeLabel(score);
        List<String> suggestions = generateSuggestions(goal.getGoalType(), totalKcal, totalProtein, totalCarbs, totalFat, goal);

        return Map.ofEntries(
            Map.entry("logCount", logs.size()),
            Map.entry("totalKcal", Math.round(totalKcal)),
            Map.entry("totalProtein", Math.round(totalProtein * 10) / 10.0),
            Map.entry("totalCarbs", Math.round(totalCarbs * 10) / 10.0),
            Map.entry("totalFat", Math.round(totalFat * 10) / 10.0),
            Map.entry("kcalPct", Math.round(kcalPct)),
            Map.entry("proteinPct", Math.round(proteinPct)),
            Map.entry("carbsPct", Math.round(carbsPct)),
            Map.entry("fatPct", Math.round(fatPct)),
            Map.entry("score", score),
            Map.entry("grade", grade),
            Map.entry("suggestions", suggestions),
            Map.entry("goal", Map.of(
                "goalType", goal.getGoalType(),
                "targetKcal", goal.getTargetKcal(),
                "targetProtein", goal.getTargetProtein(),
                "targetCarbs", goal.getTargetCarbs(),
                "targetFat", goal.getTargetFat()
            )),
            Map.entry("status", Map.of(
                "kcal", statusLabel(kcalPct, goal.getGoalType()),
                "protein", nutrientStatus(totalProtein, goal.getTargetProtein()),
                "carbs", nutrientStatus(totalCarbs, goal.getTargetCarbs()),
                "fat", nutrientStatus(totalFat, goal.getTargetFat())
            ))
        );
    }

    // ========== 评分算法 (0-100) ==========

    private int calculateScore(String goalType, int totalKcal, double protein, double carbs, double fat, UserGoal goal) {
        double score = 100;

        // 热量偏差惩罚
        double kcalDeviation = Math.abs(totalKcal - goal.getTargetKcal()) / (double)goal.getTargetKcal();
        if (kcalDeviation > 0.5) score -= 30;
        else if (kcalDeviation > 0.3) score -= 20;
        else if (kcalDeviation > 0.15) score -= 10;
        else if (kcalDeviation > 0.05) score -= 5;

        // 蛋白质偏差惩罚（蛋白质最重要）
        double proteinDeviation = Math.abs(protein - goal.getTargetProtein()) / (double)goal.getTargetProtein();
        if (proteinDeviation > 0.4) score -= 20;
        else if (proteinDeviation > 0.25) score -= 15;
        else if (proteinDeviation > 0.15) score -= 8;

        // 脂肪偏差
        double fatDeviation = goal.getTargetFat() > 0
            ? Math.abs(fat - goal.getTargetFat()) / (double)goal.getTargetFat() : 0;
        if (fatDeviation > 0.5) score -= 12;
        else if (fatDeviation > 0.3) score -= 7;
        else if (fatDeviation > 0.15) score -= 4;

        // 减脂时热量超标额外扣分
        if ("cut".equals(goalType) && totalKcal > goal.getTargetKcal()) {
            score -= 10;
        }

        return Math.max(0, Math.min(100, (int)score));
    }

    private String gradeLabel(int score) {
        if (score >= 85) return "优秀";
        if (score >= 70) return "良好";
        if (score >= 50) return "一般";
        return "不合格";
    }

    // ========== 达标分析 ==========

    private String statusLabel(double pct, String goalType) {
        if (pct >= 85 && pct <= 115) return "达标";
        if (pct < 85) return "偏低";
        return "偏高";
    }

    private String nutrientStatus(double actual, int target) {
        double pct = target > 0 ? actual / target * 100 : 0;
        if (pct >= 85 && pct <= 115) return "达标";
        if (pct < 85) return "偏低";
        return "偏高";
    }

    // ========== 规则引擎（12条规则） ==========

    private List<String> generateSuggestions(String goalType, int kcal, double protein, double carbs, double fat, UserGoal goal) {
        List<String> suggestions = new ArrayList<>();

        double kcalPct = goal.getTargetKcal() > 0 ? (double)kcal / goal.getTargetKcal() * 100 : 0;
        double proteinPct = goal.getTargetProtein() > 0 ? protein / goal.getTargetProtein() * 100 : 0;
        double carbsPct = goal.getTargetCarbs() > 0 ? carbs / goal.getTargetCarbs() * 100 : 0;
        double fatPct = goal.getTargetFat() > 0 ? fat / goal.getTargetFat() * 100 : 0;

        // 规则1：减脂目标+热量超标
        if ("cut".equals(goalType) && kcalPct > 110) {
            suggestions.add("今日热量超标，不利于减脂目标。建议减少高热量食物摄入，优先控制主食和油脂。");
        }

        // 规则2：增肌目标+热量不足
        if ("bulk".equals(goalType) && kcalPct < 90) {
            suggestions.add("今日热量摄入不足，可能影响增肌效果。建议增加一餐或加入高热量健康食物（如坚果、燕麦）。");
        }

        // 规则3：蛋白质不足
        if (proteinPct < 85) {
            suggestions.add("蛋白质摄入不足（" + Math.round(protein) + "g / 目标 " + goal.getTargetProtein() + "g）。建议增加鸡胸肉、鸡蛋、乳清蛋白或鱼肉。");
        }

        // 规则4：蛋白质充足
        if (proteinPct >= 90 && proteinPct <= 115) {
            suggestions.add("蛋白质摄入达标，有助于肌肉修复与生长。");
        }

        // 规则5：碳水偏高
        if (carbsPct > 120) {
            suggestions.add("碳水摄入偏高（" + Math.round(carbs) + "g / 目标 " + goal.getTargetCarbs() + "g）。建议减少精制碳水（米饭、面条、面包），替换为粗粮。");
        }

        // 规则6：碳水偏低
        if (carbsPct < 70) {
            suggestions.add("碳水摄入偏低，可能影响训练表现和恢复。建议增加燕麦、红薯或全麦面包等优质碳水。");
        }

        // 规则7：脂肪偏高
        if (fatPct > 130) {
            suggestions.add("脂肪摄入过高（" + Math.round(fat) + "g / 目标 " + goal.getTargetFat() + "g）。建议减少油炸食品、肥肉、坚果和油的摄入量。");
        }

        // 规则8：脂肪偏低
        if (fatPct < 50) {
            suggestions.add("脂肪摄入偏低，不利于激素分泌和脂溶性维生素吸收。建议适量增加牛油果、橄榄油或坚果。");
        }

        // 规则9：减脂+蛋白偏低
        if ("cut".equals(goalType) && proteinPct < 90) {
            suggestions.add("减脂期间蛋白质尤为关键，当前不足可能导致肌肉流失。建议每餐都保证手掌大小的蛋白质来源。");
        }

        // 规则10：增肌+碳水偏低
        if ("bulk".equals(goalType) && carbsPct < 80) {
            suggestions.add("增肌阶段碳水是训练能量的核心来源，当前偏低。建议训练前后补充碳水（如香蕉+燕麦）。");
        }

        // 规则11：全面达标
        if (kcalPct >= 85 && kcalPct <= 115 && proteinPct >= 90 && carbsPct >= 80 && carbsPct <= 120 && fatPct >= 60 && fatPct <= 130) {
            suggestions.add("今日饮食整体达标，各项营养素控制良好，继续保持！");
        }

        // 规则12：维持目标+整体合理
        if ("maintain".equals(goalType) && Math.abs(kcalPct - 100) <= 10 && proteinPct >= 85) {
            suggestions.add("热量和蛋白质均在维持目标的合理范围内，体重稳定性表现良好。");
        }

        // 无日志提示
        if (suggestions.isEmpty()) {
            suggestions.add("尚未记录今日饮食，添加食物后即可获得智能分析建议。");
        }

        return suggestions;
    }
}
