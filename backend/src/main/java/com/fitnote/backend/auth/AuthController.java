package com.fitnote.backend.auth;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.security.JwtTokenProvider;
import com.fitnote.backend.user.User;
import com.fitnote.backend.user.UserProfile;
import com.fitnote.backend.user.UserProfileRepository;
import com.fitnote.backend.user.UserRepository;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;
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

    private static final String DEFAULT_AVATAR = "https://dummyimage.com/200x200/1f1f1f/c3f400&text=F";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WeChatAuthService weChatAuthService;

    public AuthController(UserRepository userRepository,
                          UserProfileRepository userProfileRepository,
                          JwtTokenProvider jwtTokenProvider,
                          WeChatAuthService weChatAuthService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.weChatAuthService = weChatAuthService;
    }

    @PostMapping("/wechat-login")
    public ApiResponse<Map<String, Object>> wechatLogin(@RequestBody LoginRequest request) {
        WeChatAuthService.SessionInfo session = weChatAuthService.resolveSession(request.code());
        User user = userRepository.findByOpenId(session.openId()).orElseGet(() -> createDefaultUser(session, request));

        if (session.unionId() != null && (user.getUnionId() == null || user.getUnionId().isBlank())) {
            user.setUnionId(session.unionId());
            userRepository.save(user);
        }

        if (request.nickname() != null && !request.nickname().isBlank() && !request.nickname().equals(user.getNickname())) {
            user.setNickname(request.nickname());
            userRepository.save(user);
        }

        if (request.avatarUrl() != null && !request.avatarUrl().isBlank() && !request.avatarUrl().equals(user.getAvatarUrl())) {
            user.setAvatarUrl(request.avatarUrl());
            userRepository.save(user);
        }

        ensureProfile(user.getId());

        String token = jwtTokenProvider.generateUserToken(user.getId());
        return ApiResponse.ok(Map.of(
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "nickname", user.getNickname(),
                "avatarUrl", user.getAvatarUrl() == null ? "" : user.getAvatarUrl(),
                "mockLogin", weChatAuthService.isMockLogin()
            )
        ));
    }

    private User createDefaultUser(WeChatAuthService.SessionInfo session, LoginRequest request) {
        User user = new User();
        user.setOpenId(session.openId());
        user.setUnionId(session.unionId());
        user.setNickname(request.nickname() == null || request.nickname().isBlank() ? "FitNote User" : request.nickname());
        user.setAvatarUrl(request.avatarUrl() == null || request.avatarUrl().isBlank() ? DEFAULT_AVATAR : request.avatarUrl());
        return userRepository.save(user);
    }

    private void ensureProfile(Long userId) {
        if (userProfileRepository.findByUserId(userId).isPresent()) {
            return;
        }
        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setGender("Unknown");
        profile.setHeightCm(new BigDecimal("170"));
        profile.setWeightKg(new BigDecimal("65.0"));
        profile.setBodyFatRate(new BigDecimal("18.0"));
        profile.setTargetType("MuscleGain");
        profile.setTargetWeightKg(new BigDecimal("70"));
        profile.setTrainingLevel("Beginner");
        profile.setBio("Track every workout and keep growing.");
        userProfileRepository.save(profile);
    }

    public record LoginRequest(
        @NotBlank(message = "code is required")
        String code,
        String nickname,
        String avatarUrl
    ) {
    }
}
