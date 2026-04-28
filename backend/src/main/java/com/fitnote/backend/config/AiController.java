package com.fitnote.backend.config;

import com.fitnote.backend.common.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @GetMapping("/prompts")
    public ApiResponse<Map<String, Object>> prompts() {
        return ApiResponse.ok(Map.of(
            "assistantName", "Volt AI",
            "status", "已就绪，帮助你突破极限",
            "cards", List.of(
                Map.of("title", "我今天该练什么？", "type", "training"),
                Map.of("title", "练后饮食建议", "type", "nutrition"),
                Map.of("title", "恢复评估报告", "type", "recovery"),
                Map.of("title", "近 30 天进展", "type", "trend")
            ),
            "welcomeMessage", "欢迎回来。我已根据你的最近训练与恢复数据，为你准备了今日训练建议。"
        ));
    }
}
