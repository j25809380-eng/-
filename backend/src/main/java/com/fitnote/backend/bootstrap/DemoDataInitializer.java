package com.fitnote.backend.bootstrap;

import com.fitnote.backend.community.CommunityComment;
import com.fitnote.backend.community.CommunityCommentRepository;
import com.fitnote.backend.community.CommunityLike;
import com.fitnote.backend.community.CommunityLikeRepository;
import com.fitnote.backend.community.CommunityPost;
import com.fitnote.backend.community.CommunityPostRepository;
import com.fitnote.backend.exercise.Exercise;
import com.fitnote.backend.exercise.ExerciseRepository;
import com.fitnote.backend.plan.TrainingPlan;
import com.fitnote.backend.plan.TrainingPlanDay;
import com.fitnote.backend.plan.TrainingPlanDayRepository;
import com.fitnote.backend.plan.TrainingPlanItem;
import com.fitnote.backend.plan.TrainingPlanItemRepository;
import com.fitnote.backend.plan.TrainingPlanRepository;
import com.fitnote.backend.user.User;
import com.fitnote.backend.user.UserProfile;
import com.fitnote.backend.user.UserProfileRepository;
import com.fitnote.backend.user.UserRepository;
import com.fitnote.backend.workout.BodyMetric;
import com.fitnote.backend.workout.BodyMetricRepository;
import com.fitnote.backend.workout.PersonalRecord;
import com.fitnote.backend.workout.PersonalRecordRepository;
import com.fitnote.backend.workout.WorkoutSession;
import com.fitnote.backend.workout.WorkoutSessionRepository;
import com.fitnote.backend.workout.WorkoutSet;
import com.fitnote.backend.workout.WorkoutSetRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("demo")
public class DemoDataInitializer {

    @Bean
    CommandLineRunner seed(UserRepository userRepository,
                           UserProfileRepository userProfileRepository,
                           ExerciseRepository exerciseRepository,
                           TrainingPlanRepository trainingPlanRepository,
                           TrainingPlanDayRepository trainingPlanDayRepository,
                           TrainingPlanItemRepository trainingPlanItemRepository,
                           WorkoutSessionRepository workoutSessionRepository,
                           WorkoutSetRepository workoutSetRepository,
                           BodyMetricRepository bodyMetricRepository,
                           CommunityPostRepository communityPostRepository,
                           CommunityCommentRepository communityCommentRepository,
                           CommunityLikeRepository communityLikeRepository,
                           PersonalRecordRepository personalRecordRepository) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }

            User user = new User();
            user.setOpenId("wx_demo_fitnote_user");
            user.setNickname("DemoUser");
            user.setAvatarUrl("https://dummyimage.com/200x200/1f1f1f/c3f400&text=F");
            user = userRepository.save(user);

            User friend = new User();
            friend.setOpenId("wx_demo_fitnote_friend");
            friend.setNickname("Echo");
            friend.setAvatarUrl("https://dummyimage.com/200x200/161616/c3f400&text=E");
            friend = userRepository.save(friend);

            UserProfile profile = new UserProfile();
            profile.setUserId(user.getId());
            profile.setGender("Male");
            profile.setHeightCm(new BigDecimal("178"));
            profile.setWeightKg(new BigDecimal("76.5"));
            profile.setBodyFatRate(new BigDecimal("16.5"));
            profile.setTargetType("MuscleGain");
            profile.setTargetWeightKg(new BigDecimal("80.0"));
            profile.setTrainingLevel("Intermediate");
            profile.setBio("Track every workout and keep improving.");
            userProfileRepository.save(profile);

            UserProfile friendProfile = new UserProfile();
            friendProfile.setUserId(friend.getId());
            friendProfile.setGender("Female");
            friendProfile.setHeightCm(new BigDecimal("168"));
            friendProfile.setWeightKg(new BigDecimal("58.0"));
            friendProfile.setBodyFatRate(new BigDecimal("21.5"));
            friendProfile.setTargetType("LeanTone");
            friendProfile.setTargetWeightKg(new BigDecimal("56.0"));
            friendProfile.setTrainingLevel("Intermediate");
            friendProfile.setBio("Morning training enthusiast.");
            userProfileRepository.save(friendProfile);

            Exercise bench = createExercise(exerciseRepository, "Barbell Bench Press", "Chest", "Barbell", "Intermediate");
            Exercise squat = createExercise(exerciseRepository, "Barbell Squat", "Legs", "Barbell", "Intermediate");
            Exercise pullup = createExercise(exerciseRepository, "Pull Up", "Back", "Bodyweight", "Intermediate");

            TrainingPlan plan = new TrainingPlan();
            plan.setTitle("4-Day Strength Split");
            plan.setSubtitle("Upper and lower body progression");
            plan.setTargetType("MuscleGain");
            plan.setDifficulty("Intermediate");
            plan.setDurationWeeks(8);
            plan.setDaysPerWeek(4);
            plan.setSummary("Stable volume progression with compound focus.");
            plan.setCustomPlan(false);
            plan = trainingPlanRepository.save(plan);

            TrainingPlanDay day1 = new TrainingPlanDay();
            day1.setPlanId(plan.getId());
            day1.setDayNo(1);
            day1.setTitle("Push Day");
            day1.setFocus("Chest + Shoulders");
            day1 = trainingPlanDayRepository.save(day1);

            createPlanItem(trainingPlanItemRepository, day1.getId(), bench.getId(), bench.getName(), 5, "8-10", 90, "Progressive", 1);
            createPlanItem(trainingPlanItemRepository, day1.getId(), null, "Dumbbell Shoulder Press", 4, "10-12", 75, "Moderate", 2);

            TrainingPlanDay day2 = new TrainingPlanDay();
            day2.setPlanId(plan.getId());
            day2.setDayNo(2);
            day2.setTitle("Pull Day");
            day2.setFocus("Back + Arms");
            day2 = trainingPlanDayRepository.save(day2);

            createPlanItem(trainingPlanItemRepository, day2.getId(), pullup.getId(), pullup.getName(), 4, "8-10", 60, "Bodyweight", 1);
            createPlanItem(trainingPlanItemRepository, day2.getId(), squat.getId(), "Barbell Row", 4, "8-10", 90, "Progressive", 2);

            WorkoutSession session = new WorkoutSession();
            session.setUserId(user.getId());
            session.setTitle("Chest Strength Session");
            session.setFocus("Bench peak output");
            session.setSessionDate(LocalDate.now().minusDays(1));
            session.setStartedAt(LocalDateTime.now().minusDays(1).minusMinutes(68));
            session.setFinishedAt(LocalDateTime.now().minusDays(1));
            session.setDurationMinutes(68);
            session.setTotalVolume(new BigDecimal("24500"));
            session.setCalories(842);
            session.setFeelingScore(5);
            session.setNotes("Bench quality and lockout were stable.");
            session.setCompletionStatus("COMPLETED");
            session = workoutSessionRepository.save(session);

            createWorkoutSet(workoutSetRepository, session.getId(), user.getId(), bench.getId(), bench.getName(), 1, "80", 10, 2, false);
            createWorkoutSet(workoutSetRepository, session.getId(), user.getId(), bench.getId(), bench.getName(), 2, "85", 8, 1, false);
            createWorkoutSet(workoutSetRepository, session.getId(), user.getId(), bench.getId(), bench.getName(), 3, "90", 6, 0, true);
            createWorkoutSet(workoutSetRepository, session.getId(), user.getId(), squat.getId(), squat.getName(), 1, "100", 5, 1, false);

            createMetric(bodyMetricRepository, user.getId(), LocalDate.now().minusDays(21), "77.8", "17.2", "31.4");
            createMetric(bodyMetricRepository, user.getId(), LocalDate.now().minusDays(14), "77.1", "16.9", "31.8");
            createMetric(bodyMetricRepository, user.getId(), LocalDate.now().minusDays(7), "76.8", "16.7", "32.1");
            createMetric(bodyMetricRepository, user.getId(), LocalDate.now(), "76.5", "16.5", "32.4");

            createPersonalRecord(personalRecordRepository, user.getId(), bench.getId(), bench.getName(), "MAX_WEIGHT", "90", session.getId());
            createPersonalRecord(personalRecordRepository, user.getId(), squat.getId(), squat.getName(), "MAX_WEIGHT", "100", session.getId());

            CommunityPost post = new CommunityPost();
            post.setUserId(user.getId());
            post.setAuthorName(user.getNickname());
            post.setAuthorAvatar(user.getAvatarUrl());
            post.setContent("Hit a new bench PR today. Next target: stable 5x5.");
            post.setPostType("PR");
            post.setTopicTags("checkin,bench,pr");
            post.setCollectCount(6);
            post.setAuditStatus("APPROVED");
            post = communityPostRepository.save(post);

            CommunityPost pendingPost = new CommunityPost();
            pendingPost.setUserId(user.getId());
            pendingPost.setAuthorName("PendingDemo");
            pendingPost.setAuthorAvatar(user.getAvatarUrl());
            pendingPost.setContent("This post stays pending for admin moderation demo.");
            pendingPost.setPostType("TRAINING");
            pendingPost.setTopicTags("pending,demo");
            pendingPost.setLikeCount(12);
            pendingPost.setCommentCount(2);
            pendingPost.setCollectCount(1);
            pendingPost.setAuditStatus("PENDING");
            communityPostRepository.save(pendingPost);

            createComment(communityCommentRepository, post.getId(), friend.getId(), friend.getNickname(), "Great control and stable reps!");
            createLike(communityLikeRepository, post.getId(), friend.getId());

            post.setCommentCount((int) communityCommentRepository.findByPostIdOrderByCreatedAtAsc(post.getId()).size());
            post.setLikeCount((int) communityLikeRepository.countByPostId(post.getId()));
            communityPostRepository.save(post);
        };
    }

    private Exercise createExercise(ExerciseRepository repository,
                                    String name,
                                    String category,
                                    String equipment,
                                    String difficulty) {
        Exercise exercise = new Exercise();
        exercise.setName(name);
        exercise.setCategory(category);
        exercise.setEquipment(equipment);
        exercise.setDifficulty(difficulty);
        exercise.setPrimaryMuscle(category);
        exercise.setSecondaryMuscles("");
        exercise.setDescription(name + " demo description");
        exercise.setMovementSteps("Step 1\nStep 2\nStep 3");
        exercise.setTips("Keep stable tempo and full control.");
        exercise.setCoverImage("");
        return repository.save(exercise);
    }

    private void createPlanItem(TrainingPlanItemRepository repository,
                                Long dayId,
                                Long exerciseId,
                                String name,
                                int sets,
                                String reps,
                                int rest,
                                String weightMode,
                                int sortNo) {
        TrainingPlanItem item = new TrainingPlanItem();
        item.setDayId(dayId);
        item.setExerciseId(exerciseId);
        item.setExerciseName(name);
        item.setSetsCount(sets);
        item.setReps(reps);
        item.setRestSeconds(rest);
        item.setWeightMode(weightMode);
        item.setSortNo(sortNo);
        repository.save(item);
    }

    private void createWorkoutSet(WorkoutSetRepository repository,
                                  Long sessionId,
                                  Long userId,
                                  Long exerciseId,
                                  String name,
                                  int setNo,
                                  String weight,
                                  int reps,
                                  int rir,
                                  boolean isPr) {
        WorkoutSet set = new WorkoutSet();
        set.setSessionId(sessionId);
        set.setUserId(userId);
        set.setExerciseId(exerciseId);
        set.setExerciseName(name);
        set.setSetNo(setNo);
        set.setWeightKg(new BigDecimal(weight));
        set.setReps(reps);
        set.setRir(rir);
        set.setPr(isPr);
        repository.save(set);
    }

    private void createMetric(BodyMetricRepository repository,
                              Long userId,
                              LocalDate date,
                              String weight,
                              String bodyFat,
                              String muscle) {
        BodyMetric metric = new BodyMetric();
        metric.setUserId(userId);
        metric.setMetricDate(date);
        metric.setWeightKg(new BigDecimal(weight));
        metric.setBodyFatRate(new BigDecimal(bodyFat));
        metric.setSkeletalMuscleKg(new BigDecimal(muscle));
        repository.save(metric);
    }

    private void createPersonalRecord(PersonalRecordRepository repository,
                                      Long userId,
                                      Long exerciseId,
                                      String exerciseName,
                                      String recordType,
                                      String recordValue,
                                      Long sessionId) {
        PersonalRecord record = new PersonalRecord();
        record.setUserId(userId);
        record.setExerciseId(exerciseId);
        record.setExerciseName(exerciseName);
        record.setRecordType(recordType);
        record.setRecordValue(new BigDecimal(recordValue));
        record.setAchievedAt(LocalDateTime.now().minusDays(1));
        record.setSourceSessionId(sessionId);
        repository.save(record);
    }

    private void createComment(CommunityCommentRepository repository,
                               Long postId,
                               Long userId,
                               String authorName,
                               String content) {
        CommunityComment comment = new CommunityComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setAuthorName(authorName);
        comment.setContent(content);
        repository.save(comment);
    }

    private void createLike(CommunityLikeRepository repository,
                            Long postId,
                            Long userId) {
        CommunityLike like = new CommunityLike();
        like.setPostId(postId);
        like.setUserId(userId);
        repository.save(like);
    }
}
