package com.fitnote.backend.nutrition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.fitnote.backend.user.UserProfile;
import com.fitnote.backend.user.UserProfileRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NutritionServiceTest {

    @Mock
    private UserGoalRepository userGoalRepository;

    @Mock
    private DietLogRepository dietLogRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private NutritionService nutritionService;

    private UserProfile profile;

    @BeforeEach
    void setUp() {
        profile = new UserProfile();
        profile.setUserId(1L);
        profile.setWeightKg(new BigDecimal("75"));
        profile.setBodyFatRate(new BigDecimal("15"));
        profile.setTargetType("增肌");
    }

    @Test
    void getGoal_shouldReturnExistingGoal() {
        UserGoal existing = new UserGoal();
        existing.setUserId(1L);
        existing.setGoalType("maintain");
        when(userGoalRepository.findByUserId(1L)).thenReturn(Optional.of(existing));

        UserGoal result = nutritionService.getGoal(1L);
        assertNotNull(result);
        assertEquals("maintain", result.getGoalType());
    }

    @Test
    void getGoal_shouldCreateDefaultWhenNotFound() {
        when(userGoalRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        UserGoal result = nutritionService.getGoal(1L);
        assertNotNull(result);
        assertEquals("maintain", result.getGoalType());
    }

    @Test
    void updateGoal_bulk_shouldIncreaseCalories() {
        UserGoal goal = new UserGoal();
        goal.setUserId(1L);
        goal.setGoalType("maintain");
        when(userGoalRepository.findByUserId(1L)).thenReturn(Optional.of(goal));
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        UserGoal result = nutritionService.updateGoal(1L, "bulk");
        assertEquals("bulk", result.getGoalType());
        assertTrue(result.getTargetProtein() > 100);
    }

    @Test
    void updateGoal_cut_shouldReduceCalories() {
        UserGoal goal = new UserGoal();
        goal.setUserId(1L);
        goal.setGoalType("maintain");
        when(userGoalRepository.findByUserId(1L)).thenReturn(Optional.of(goal));
        when(userProfileRepository.findByUserId(1L)).thenReturn(Optional.of(profile));
        when(userGoalRepository.save(any(UserGoal.class))).thenAnswer(inv -> inv.getArgument(0));

        UserGoal result = nutritionService.updateGoal(1L, "cut");
        assertEquals("cut", result.getGoalType());
    }
}
