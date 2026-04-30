package com.fitnote.backend.auth;

import com.fitnote.backend.common.ApiResponse;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/wechat-login")
    public ApiResponse<Map<String, Object>> wechatLogin(@RequestBody LoginRequest request) {
        Map<String, Object> result = authService.wechatLogin(
            request.code(), request.nickname(), request.avatarUrl());
        return ApiResponse.ok(result);
    }

    public record LoginRequest(
        @NotBlank(message = "code is required")
        String code,
        String nickname,
        String avatarUrl
    ) {
    }
}
