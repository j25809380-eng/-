package com.fitnote.backend.admin;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import com.fitnote.backend.security.JwtTokenProvider;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final SysAdminRepository sysAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AdminAuthController(SysAdminRepository sysAdminRepository,
                               PasswordEncoder passwordEncoder,
                               JwtTokenProvider jwtTokenProvider) {
        this.sysAdminRepository = sysAdminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody AdminLoginRequest request) {
        SysAdmin admin = sysAdminRepository.findByUsername(request.username()).orElseThrow(() -> new IllegalArgumentException("管理员不存在"));
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new IllegalStateException("管理员账号已禁用");
        }
        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            throw new IllegalArgumentException("账号或密码错误");
        }

        String token = jwtTokenProvider.generateAdminToken(admin.getId(), admin.getNickname());
        return ApiResponse.ok(Map.of(
            "token", token,
            "admin", Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "nickname", admin.getNickname(),
                "roleCode", admin.getRoleCode()
            )
        ));
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        Long adminId = CurrentUser.id();
        SysAdmin admin = sysAdminRepository.findById(adminId).orElseThrow();
        return ApiResponse.ok(Map.of(
            "id", admin.getId(),
            "username", admin.getUsername(),
            "nickname", admin.getNickname(),
            "roleCode", admin.getRoleCode()
        ));
    }

    public record AdminLoginRequest(
        @NotBlank String username,
        @NotBlank String password
    ) {
    }
}
