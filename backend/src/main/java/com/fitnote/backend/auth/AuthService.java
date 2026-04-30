package com.fitnote.backend.auth;

import com.fitnote.backend.security.JwtTokenProvider;
import com.fitnote.backend.user.User;
import com.fitnote.backend.user.UserProfile;
import com.fitnote.backend.user.UserProfileRepository;
import com.fitnote.backend.user.UserRepository;
import java.math.BigDecimal;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String DEFAULT_AVATAR = "https://dummyimage.com/200x200/1f1f1f/c3f400&text=F";

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final WeChatAuthService weChatAuthService;

    public AuthService(UserRepository userRepository,
                       UserProfileRepository userProfileRepository,
                       JwtTokenProvider jwtTokenProvider,
                       WeChatAuthService weChatAuthService) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.weChatAuthService = weChatAuthService;
    }

    @Transactional
    public Map<String, Object> wechatLogin(String code, String nickname, String avatarUrl) {
        WeChatAuthService.SessionInfo session = weChatAuthService.resolveSession(code);
        User user = userRepository.findByOpenId(session.openId())
            .orElseGet(() -> createDefaultUser(session, nickname, avatarUrl));

        if (session.unionId() != null && (user.getUnionId() == null || user.getUnionId().isBlank())) {
            user.setUnionId(session.unionId());
            userRepository.save(user);
        }

        if (nickname != null && !nickname.isBlank() && !nickname.equals(user.getNickname())) {
            user.setNickname(nickname);
            userRepository.save(user);
        }

        if (avatarUrl != null && !avatarUrl.isBlank() && !avatarUrl.equals(user.getAvatarUrl())) {
            user.setAvatarUrl(avatarUrl);
            userRepository.save(user);
        }

        ensureProfile(user.getId());

        String token = jwtTokenProvider.generateUserToken(user.getId());
        return Map.of(
            "token", token,
            "user", Map.of(
                "id", user.getId(),
                "nickname", user.getNickname(),
                "avatarUrl", user.getAvatarUrl() == null ? "" : user.getAvatarUrl(),
                "mockLogin", weChatAuthService.isMockLogin()
            )
        );
    }

    private User createDefaultUser(WeChatAuthService.SessionInfo session, String nickname, String avatarUrl) {
        User user = new User();
        user.setOpenId(session.openId());
        user.setUnionId(session.unionId());
        user.setNickname(nickname == null || nickname.isBlank() ? "FitNote User" : nickname);
        user.setAvatarUrl(avatarUrl == null || avatarUrl.isBlank() ? DEFAULT_AVATAR : avatarUrl);
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
}
