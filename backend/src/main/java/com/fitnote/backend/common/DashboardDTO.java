package com.fitnote.backend.common;

import java.util.List;
import java.util.Map;

public record DashboardDTO(
    HeroDTO hero,
    ReadinessDTO readiness,
    OverviewDTO overview,
    List<PlanDTO> quickPlans,
    List<ActionDTO> quickActions
) {
    public record HeroDTO(String title, String subtitle, String targetType, String trainingLevel) {}
    public record ReadinessDTO(String mealReady, String hydration, String recovery) {}
    public record OverviewDTO(long weeklySessions, long activePlans, Object goalWeight) {}
    public record PlanDTO(long id, String title, String difficulty, int durationWeeks) {}
    public record ActionDTO(String name, String path) {}

    @SuppressWarnings("unchecked")
    public static DashboardDTO fromMap(Map<String, Object> map) {
        Map<String, Object> hero = (Map<String, Object>) map.get("hero");
        Map<String, Object> readiness = (Map<String, Object>) map.get("readiness");
        Map<String, Object> overview = (Map<String, Object>) map.get("overview");
        List<Map<String, Object>> quickPlans = (List<Map<String, Object>>) map.get("quickPlans");
        List<Map<String, Object>> quickActions = (List<Map<String, Object>>) map.get("quickActions");

        return new DashboardDTO(
            new HeroDTO(s(hero, "title"), s(hero, "subtitle"), s(hero, "targetType"), s(hero, "trainingLevel")),
            new ReadinessDTO(s(readiness, "mealReady"), s(readiness, "hydration"), s(readiness, "recovery")),
            new OverviewDTO(n(overview, "weeklySessions"), n(overview, "activePlans"), overview.get("goalWeight")),
            quickPlans.stream().map(p -> new PlanDTO(n(p, "id"), s(p, "title"), s(p, "difficulty"), (int) n(p, "durationWeeks"))).toList(),
            quickActions.stream().map(a -> new ActionDTO(s(a, "name"), s(a, "path"))).toList()
        );
    }

    private static String s(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        return v != null ? String.valueOf(v) : "";
    }

    private static long n(Map<String, Object> m, String k) {
        Object v = m != null ? m.get(k) : null;
        if (v instanceof Number n) return n.longValue();
        return 0;
    }
}
