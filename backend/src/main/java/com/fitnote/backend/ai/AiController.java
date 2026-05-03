package com.fitnote.backend.ai;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/prompts")
    public ApiResponse<Map<String, Object>> prompts() {
        return ApiResponse.ok(aiService.getPrompts());
    }

    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody ChatRequest request) {
        if (request.message() == null || request.message().isBlank()) {
            return new ApiResponse<>(400, "消息不能为空", null);
        }
        return ApiResponse.ok(aiService.chat(CurrentUser.id(), request.message()));
    }

    public record ChatRequest(String message) {}
}
