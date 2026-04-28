package com.fitnote.backend.user;

import com.fitnote.backend.common.ApiResponse;
import com.fitnote.backend.common.CurrentUser;
import com.fitnote.backend.workout.BodyMetricRepository;
import com.fitnote.backend.workout.PersonalRecordRepository;
import com.fitnote.backend.workout.WorkoutSessionRepository;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final BodyMetricRepository bodyMetricRepository;
    private final PersonalRecordRepository personalRecordRepository;

    public UserController(UserRepository userRepository,
                          UserProfileRepository userProfileRepository,
                          WorkoutSessionRepository workoutSessionRepository,
                          BodyMetricRepository bodyMetricRepository,
                          PersonalRecordRepository personalRecordRepository) {
        this.userRepository = userRepository;
        this.userProfileRepository = userProfileRepository;
        this.workoutSessionRepository = workoutSessionRepository;
        this.bodyMetricRepository = bodyMetricRepository;
        this.personalRecordRepository = personalRecordRepository;
    }

    @GetMapping("/users/me")
    public ApiResponse<Map<String, Object>> me() {
        Long userId = CurrentUser.id();
        User user = userRepository.findById(userId).orElseThrow();
        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSessions", workoutSessionRepository.findByUserIdOrderBySessionDateDesc(userId).size());
        stats.put("prCount", personalRecordRepository.findByUserIdOrderByAchievedAtDesc(userId).size());
        stats.put("bodyMetricCount", bodyMetricRepository.findByUserIdOrderByMetricDateAsc(userId).size());

        return ApiResponse.ok(Map.of(
            "user", Map.of(
                "id", user.getId(),
                "nickname", user.getNickname(),
                "avatarUrl", user.getAvatarUrl(),
                "phone", user.getPhone() == null ? "" : user.getPhone()
            ),
            "profile", Map.of(
                "gender", profile.getGender(),
                "heightCm", profile.getHeightCm(),
                "weightKg", profile.getWeightKg(),
                "bodyFatRate", profile.getBodyFatRate(),
                "targetType", profile.getTargetType(),
                "targetWeightKg", profile.getTargetWeightKg(),
                "trainingLevel", profile.getTrainingLevel(),
                "bio", profile.getBio()
            ),
            "stats", stats
        ));
    }

    @PutMapping("/users/me/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = CurrentUser.id();
        User user = userRepository.findById(userId).orElseThrow();
        UserProfile profile = userProfileRepository.findByUserId(userId).orElseThrow();

        user.setNickname(request.nickname());
        if (request.avatarUrl() != null && !request.avatarUrl().isBlank()) {
            user.setAvatarUrl(request.avatarUrl());
        }
        userRepository.save(user);

        profile.setGender(request.gender());
        profile.setHeightCm(request.heightCm());
        profile.setWeightKg(request.weightKg());
        profile.setBodyFatRate(request.bodyFatRate());
        profile.setTargetType(request.targetType());
        profile.setTargetWeightKg(request.targetWeightKg());
        profile.setTrainingLevel(request.trainingLevel());
        profile.setBio(request.bio());
        userProfileRepository.save(profile);

        return ApiResponse.ok(Map.of("updated", true));
    }

    @PostMapping("/files/avatar")
    public ApiResponse<Map<String, Object>> uploadAvatar(@RequestPart("file") MultipartFile file,
                                                         @RequestParam(required = false) String fileName) {
        Long userId = CurrentUser.id();
        String originalName = (fileName == null || fileName.isBlank()) ? file.getOriginalFilename() : fileName;
        String extension = originalName != null && originalName.contains(".")
            ? originalName.substring(originalName.lastIndexOf('.'))
            : ".png";
        String safeName = "avatar-" + userId + "-" + System.currentTimeMillis() + extension;
        try {
            Path uploadDir = Paths.get("uploads");
            Files.createDirectories(uploadDir);
            Path target = uploadDir.resolve(safeName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            throw new RuntimeException("头像上传失败", ex);
        }
        User user = userRepository.findById(userId).orElseThrow();
        user.setAvatarUrl("/uploads/" + safeName);
        userRepository.save(user);
        return ApiResponse.ok(Map.of(
            "fileName", safeName,
            "url", "/uploads/" + safeName
        ));
    }

    public record UpdateProfileRequest(
        String nickname,
        String gender,
        BigDecimal heightCm,
        BigDecimal weightKg,
        BigDecimal bodyFatRate,
        String targetType,
        BigDecimal targetWeightKg,
        String trainingLevel,
        String bio,
        String avatarUrl
    ) {
    }
}
