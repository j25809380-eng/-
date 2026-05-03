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
import java.util.List;
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

            // ========== 用户 ==========
            User user = new User();
            user.setOpenId("wx_demo_fitnote_user");
            user.setNickname("林燃");
            user.setAvatarUrl("https://dummyimage.com/200x200/1f1f1f/c3f400&text=F");
            user = userRepository.save(user);

            User friend = new User();
            friend.setOpenId("wx_demo_fitnote_friend");
            friend.setNickname("Echo");
            friend.setAvatarUrl("https://dummyimage.com/200x200/161616/c3f400&text=E");
            friend = userRepository.save(friend);

            UserProfile profile = new UserProfile();
            profile.setUserId(user.getId());
            profile.setGender("男");
            profile.setHeightCm(new BigDecimal("178"));
            profile.setWeightKg(new BigDecimal("76.5"));
            profile.setBodyFatRate(new BigDecimal("16.5"));
            profile.setTargetType("增肌");
            profile.setTargetWeightKg(new BigDecimal("80.0"));
            profile.setTrainingLevel("进阶");
            profile.setBio("每一次训练，都是写给未来自己的证明。");
            userProfileRepository.save(profile);

            UserProfile friendProfile = new UserProfile();
            friendProfile.setUserId(friend.getId());
            friendProfile.setGender("女");
            friendProfile.setHeightCm(new BigDecimal("168"));
            friendProfile.setWeightKg(new BigDecimal("58.0"));
            friendProfile.setBodyFatRate(new BigDecimal("21.5"));
            friendProfile.setTargetType("减脂");
            friendProfile.setTargetWeightKg(new BigDecimal("54.0"));
            friendProfile.setTrainingLevel("中级");
            friendProfile.setBio("清晨训练爱好者。");
            userProfileRepository.save(friendProfile);

            // ========== 动作库（中文、完整字段） ==========

            // ---- 胸部 ----
            Exercise bench = createExercise(exerciseRepository,
                "杠铃卧推", "胸部", "杠铃", "进阶", "胸大肌", "三角肌前束,肱三头肌", true, 10, "新手,进阶,高级",
                "杠铃卧推是上肢力量的王牌动作，能最大化刺激胸大肌、三角肌前束和肱三头肌。",
                "仰卧于平板凳，双脚踩实\n肩胛骨收紧下沉\n握距约1.5倍肩宽\n下放杠铃至胸中部\n爆发推起至肘关节锁定",
                "全程保持肩胛骨收紧\n下落时肘部与躯干约45°角\n避免臀部离开凳面");

            Exercise incDumbbell = createExercise(exerciseRepository,
                "上斜哑铃卧推", "胸部", "哑铃", "中级", "胸大肌上部", "三角肌前束,肱三头肌", true, 8, "新手,进阶,高级",
                "针对上胸的经典动作，哑铃可提供更大的活动范围。",
                "调节凳面至30-45°\n双手各持哑铃\n推起至肘关节接近锁定\n控制下落至胸上部两侧",
                "角度不宜超过45°，否则肩部受力过多\n下落时感受胸肌拉伸");

            Exercise cableFly = createExercise(exerciseRepository,
                "龙门架夹胸", "胸部", "龙门架", "初级", "胸大肌", "三角肌前束", false, 6, "新手,进阶",
                "孤立刺激胸大肌的塑形动作，适合在推类动作后进行。",
                "将滑轮调至与肩同高\n双手抓握手柄\n微曲肘部向前夹胸\n顶峰收缩1秒后缓慢还原",
                "保持肘部角度不变\n动作过程匀速控制\n顶峰收缩是关键");

            Exercise dip = createExercise(exerciseRepository,
                "双杠臂屈伸", "胸部", "双杠", "进阶", "胸大肌下部", "肱三头肌,三角肌前束", true, 7, "进阶,高级",
                "下胸王牌动作，同时强力刺激肱三头肌。",
                "双手撑于双杠\n身体前倾约30°\n屈肘下放至肩部低于肘部\n发力推起至手臂伸直",
                "身体前倾越多，胸部刺激越大\n下落深度根据肩关节灵活度调整");

            Exercise pushUp = createExercise(exerciseRepository,
                "俯卧撑", "胸部", "自重", "初级", "胸大肌", "三角肌前束,肱三头肌,核心", true, 9, "新手,进阶,高级",
                "最经典的自重训练动作，适合任何场景。",
                "双手略宽于肩撑地\n身体呈一条直线\n屈肘下放至胸部接近地面\n发力推起至手臂伸直",
                "核心全程收紧\n避免塌腰或撅臀\n可调整手间距改变刺激重点");

            // ---- 背部 ----
            Exercise pullUp = createExercise(exerciseRepository,
                "引体向上", "背部", "单杠", "进阶", "背阔肌", "肱二头肌,大圆肌,斜方肌", true, 10, "进阶,高级",
                "背部训练黄金动作，打造V型背部的核心。",
                "正握单杠，握距略宽于肩\n肩胛骨下沉启动\n发力上拉至下巴过杠\n控制下放至手臂伸直",
                "避免借力摆动\n下拉时感受背部发力而非手臂\n可辅助带减轻难度");

            Exercise bbRow = createExercise(exerciseRepository,
                "杠铃划船", "背部", "杠铃", "进阶", "背阔肌", "斜方肌中下束,肱二头肌", true, 9, "进阶,高级",
                "增加背部厚度的核心动作。",
                "双脚与肩同宽，俯身约45°\n正握杠铃\n将杠铃沿大腿拉向下腹\n顶峰收缩后控制下放",
                "保持腰背挺直\n核心全程收紧\n避免上体过度抬起借力");

            Exercise seatedRow = createExercise(exerciseRepository,
                "坐姿划船", "背部", "龙门架", "中级", "背阔肌", "菱形肌,斜方肌中束,肱二头肌", false, 7, "新手,进阶",
                "背部厚度训练的优质选择，对腰椎友好。",
                "坐于划船凳，双脚踩踏板\n双手抓握V柄\n身体挺直，将手柄拉向腹部\n顶峰收缩后缓慢还原",
                "避免身体过度后仰\n感受背部发力而非手臂");

            Exercise latPullDown = createExercise(exerciseRepository,
                "高位下拉", "背部", "龙门架", "初级", "背阔肌", "大圆肌,肱二头肌", true, 8, "新手,进阶,高级",
                "引体向上的优质替代动作，可自由调节重量。",
                "坐于下拉架，大腿固定\n宽握横杆\n肩胛骨下沉启动\n将横杆拉至上胸位置",
                "避免身体过度后仰\n下拉时挺胸感受背部挤压");

            // ---- 腿部 ----
            Exercise squat = createExercise(exerciseRepository,
                "杠铃深蹲", "腿部", "杠铃", "进阶", "股四头肌", "臀大肌,腘绳肌,竖脊肌", true, 10, "进阶,高级",
                "下肢力量之王，全身性的复合动作。",
                "杠铃置于斜方肌上\n双脚与肩同宽或略宽\n吸气下蹲至大腿与地面平行\n脚跟发力站起",
                "膝盖方向与脚尖一致\n核心全程收紧\n避免塌腰和重心前移");

            Exercise legPress = createExercise(exerciseRepository,
                "腿举", "腿部", "器械", "初级", "股四头肌", "臀大肌,腘绳肌", true, 7, "新手,进阶,高级",
                "安全的腿部复合动作，适合大重量训练。",
                "坐于腿举机，双脚与肩同宽\n解锁安全杆\n屈膝下放至90°\n发力推起至膝盖接近伸直",
                "不要完全锁死膝盖\n全程控制节奏\n腰部紧贴靠垫");

            Exercise lunge = createExercise(exerciseRepository,
                "哑铃弓步蹲", "腿部", "哑铃", "中级", "股四头肌", "臀大肌,腘绳肌", true, 6, "新手,进阶,高级",
                "改善腿部平衡和稳定性的功能性动作。",
                "双手持哑铃于体侧\n向前迈一大步\n后膝接近地面但不触地\n前脚发力站起还原",
                "保持上半身挺直\n前膝不超过脚尖\n控制节奏，避免惯性");

            Exercise rdl = createExercise(exerciseRepository,
                "罗马尼亚硬拉", "腿部", "杠铃", "进阶", "腘绳肌", "臀大肌,竖脊肌", true, 8, "进阶,高级",
                "腘绳肌和臀部训练的王牌动作。",
                "双脚与髋同宽\n正握杠铃于体前\n微曲膝，以髋为轴前倾\n杠铃沿腿下放至小腿中段",
                "腰背始终挺直\n感受腘绳肌拉伸\n膝盖角度保持不变");

            Exercise legCurl = createExercise(exerciseRepository,
                "俯卧腿弯举", "腿部", "器械", "初级", "腘绳肌", "腓肠肌", false, 5, "新手,进阶",
                "孤立腘绳肌的经典动作。",
                "俯卧于腿弯举机\n脚跟勾住滚轴\n弯曲膝盖将滚轴拉向臀部\n顶峰收缩后缓慢还原",
                "控制离心阶段\n避免腰部过度反弓\n全程匀速");

            // ---- 肩部 ----
            Exercise ohp = createExercise(exerciseRepository,
                "杠铃推举", "肩部", "杠铃", "进阶", "三角肌前束", "三角肌中束,肱三头肌,斜方肌", true, 9, "进阶,高级",
                "肩部力量和维度的核心动作。",
                "坐姿或站姿，杠铃置于锁骨前\n双手略宽于肩\n垂直向上推至头顶上方\n控制下放至起始位置",
                "核心收紧保持稳定\n避免腰部过度反弓\n全程在控制中完成");

            Exercise lateralRaise = createExercise(exerciseRepository,
                "哑铃侧平举", "肩部", "哑铃", "初级", "三角肌中束", "斜方肌上束", false, 8, "新手,进阶,高级",
                "打造肩宽的经典孤立动作。",
                "双手持哑铃垂于体侧\n微曲肘部\n向两侧平举至与肩同高\n顶峰收缩后缓慢下放",
                "不要借力摆动\n重量不宜过大\n控制离心阶段4秒");

            Exercise rearDelt = createExercise(exerciseRepository,
                "面拉", "肩部", "龙门架", "中级", "三角肌后束", "菱形肌,肩袖肌群", false, 7, "新手,进阶,高级",
                "改善肩部健康和体态的重要动作。",
                "将滑轮调至面部高度\n双手抓握绳索\n向面部拉动绳索\n顶峰收缩1-2秒后还原",
                "肘部向外打开\n感受三角肌后束发力\n对肩关节健康有益");

            // ---- 手臂 ----
            Exercise barbellCurl = createExercise(exerciseRepository,
                "杠铃弯举", "手臂", "杠铃", "初级", "肱二头肌", "肱肌,前臂", false, 6, "新手,进阶,高级",
                "训练肱二头肌的基础动作。",
                "双手与肩同宽正握杠铃\n上臂紧贴体侧\n弯举杠铃至肩部高度\n顶峰收缩后缓慢下放",
                "避免借力摆动\n全程控制节奏\n离心阶段不要自由落体");

            Exercise skullCrusher = createExercise(exerciseRepository,
                "仰卧臂屈伸", "手臂", "EZ杠", "中级", "肱三头肌", "肘肌", false, 6, "进阶,高级",
                "肱三头肌增肌的核心动作。",
                "仰卧于平板凳\n双手窄握EZ杠\n屈肘将杠铃下放至额头前\n发力伸直手臂还原",
                "肘部保持固定位置\n不要使用过重重量\n保护肘关节");

            // ---- 核心 ----
            Exercise plank = createExercise(exerciseRepository,
                "平板支撑", "核心", "自重", "初级", "腹直肌", "腹横肌,竖脊肌,肩胛肌群", false, 7, "新手,进阶,高级",
                "核心稳定性的基础训练动作。",
                "前臂撑地，肘在肩正下方\n身体呈一条直线\n收紧腹部和臀部\n保持稳定呼吸",
                "不要塌腰或撅臀\n从30秒开始逐渐增加时长\n配合呼吸节奏");

            Exercise cableCrunch = createExercise(exerciseRepository,
                "龙门架卷腹", "核心", "龙门架", "中级", "腹直肌", "腹斜肌", false, 5, "新手,进阶,高级",
                "可负重练腹的高效动作。",
                "跪姿面对龙门架\n双手抓握绳索于颈后\n卷曲躯干使肘部接近膝盖\n顶峰收缩后缓慢还原",
                "用腹肌发力而非手臂\n动作幅度不宜过大\n配合呼气收缩");

            // ---- 全身动作 ----
            Exercise deadlift = createExercise(exerciseRepository,
                "传统硬拉", "背部", "杠铃", "高级", "竖脊肌", "臀大肌,腘绳肌,斜方肌,前臂", true, 10, "进阶,高级",
                "全身力量之王，测试整体力量水平的标志性动作。",
                "双脚与髋同宽，杠铃贴小腿\n正反握或正握杠铃\n腰背挺直，以髋膝联动拉起\n锁定髋膝后控制下放",
                "腰背全程挺直不可弓背\n杠铃全程贴腿\n启动时先收紧背部和核心");

            Exercise hangClean = createExercise(exerciseRepository,
                "高翻", "全身", "杠铃", "高级", "全身爆发力", "斜方肌,股四头肌,臀大肌,三角肌", true, 5, "高级",
                "爆发力训练的经典奥林匹克举重动作。",
                "双手握杠铃于膝上\n快速伸髋伸膝发力\n耸肩提肘接杠\n前蹲姿势接住杠铃",
                "需在教练指导下进行\n核心收紧全程发力\n技术优先于重量");

            // ---- 有氧/其他 ----
            Exercise burpee = createExercise(exerciseRepository,
                "波比跳", "全身", "自重", "中级", "全身", "股四头肌,胸大肌,核心", true, 5, "新手,进阶,高级",
                "全身性高强度动作，燃脂利器。",
                "站立位开始\n下蹲手撑地跳至平板\n做一个俯卧撑\n跳回收腿后垂直起跳",
                "保持动作连贯\n根据体能调整节奏\n可简化去掉俯卧撑和跳跃");

            // ---- 手臂扩展 ----
            Exercise hammerCurl = createExercise(exerciseRepository,
                "锤式弯举", "手臂", "哑铃", "初级", "肱肌", "肱二头肌,肱桡肌", false, 6, "新手,进阶,高级",
                "针对肱肌和前臂的弯举变式，增加手臂厚度。",
                "双手持哑铃掌心相对\n上臂紧贴体侧\n弯举至肩部高度\n控制下放",
                "掌心始终相对\n避免身体摆动借力");

            Exercise preacherCurl = createExercise(exerciseRepository,
                "牧师凳弯举", "手臂", "哑铃", "中级", "肱二头肌", "肱肌", false, 6, "进阶,高级",
                "消除借力的孤立弯举，最大化肱二头肌刺激。",
                "坐于牧师凳，腋窝卡住凳面\n单手持哑铃\n弯举至肩高\n顶峰收缩1-2秒后下放",
                "手臂完全伸直获得最大拉伸\n离心阶段4秒控制");

            Exercise tricepPushdown = createExercise(exerciseRepository,
                "绳索下压", "手臂", "龙门架", "初级", "肱三头肌", "肘肌", false, 6, "新手,进阶,高级",
                "肱三头肌经典孤立动作。",
                "面对龙门架，滑轮置于高位\n双手抓握绳索\n肘部固定于体侧\n下压至手臂伸直",
                "肘部全程紧贴体侧\n顶峰时用力收缩三头肌");

            // ---- 背部扩展 ----
            Exercise oneArmRow = createExercise(exerciseRepository,
                "单臂哑铃划船", "背部", "哑铃", "中级", "背阔肌", "菱形肌,斜方肌中束,肱二头肌", true, 8, "新手,进阶,高级",
                "单侧背部训练经典动作，改善左右不对称。",
                "一侧手膝撑凳，另一手持哑铃\n腰背挺直\n将哑铃拉向髋部\n顶峰收缩后控制下放",
                "感受背阔肌发力而非手臂\n上体保持稳定不旋转");

            Exercise straightArmPulldown = createExercise(exerciseRepository,
                "直臂下拉", "背部", "龙门架", "初级", "背阔肌", "大圆肌,肱三头肌长头", false, 5, "新手,进阶,高级",
                "背部训练的优质辅助动作，强化背阔肌下部。",
                "面对龙门架，滑轮置于高位\n双手抓握横杆，手臂伸直\n直臂将横杆下压至大腿前\n控制还原至起始",
                "手臂全程保持伸直\n用背部发力下压");

            // ---- 腿部扩展 ----
            Exercise bulgarianSplit = createExercise(exerciseRepository,
                "保加利亚分腿蹲", "腿部", "哑铃", "中级", "股四头肌", "臀大肌,腘绳肌,核心", true, 8, "进阶,高级",
                "单侧腿部训练王者，对平衡和稳定性要求高。",
                "后脚置于凳面，前脚向前\n双手持哑铃于体侧\n屈前膝下蹲至大腿平行\n前脚发力站起",
                "前膝不超过脚尖\n身体保持挺直\n控制离心4秒");

            Exercise calfRaise = createExercise(exerciseRepository,
                "站姿提踵", "腿部", "哑铃", "初级", "腓肠肌", "比目鱼肌", false, 5, "新手,进阶,高级",
                "小腿肌肉的基础训练动作。",
                "双脚与髋同宽站立\n可手持哑铃增加负重\n踮脚至最高点\n顶峰收缩1秒后缓慢下放",
                "动作全程匀速控制\n下落时感受小腿拉伸");

            // ---- 核心扩展 ----
            Exercise hangingLegRaise = createExercise(exerciseRepository,
                "悬垂举腿", "核心", "单杠", "进阶", "腹直肌", "腹斜肌,髋屈肌", true, 7, "进阶,高级",
                "核心训练顶级动作，对握力也有要求。",
                "双手正握悬垂于单杠\n屈髋抬腿至腿与地面平行\n顶峰收缩后控制下放",
                "避免借力摆动\n若力量不足可屈膝完成");

            Exercise russianTwist = createExercise(exerciseRepository,
                "俄罗斯转体", "核心", "自重", "初级", "腹斜肌", "腹直肌", false, 5, "新手,进阶,高级",
                "针对腹斜肌的经典核心训练动作。",
                "坐姿屈膝，脚离地\n身体后倾约45°\n双手合十左右转体\n匀速交替",
                "核心全程收紧\n可持重物增加难度");

            Exercise deadBug = createExercise(exerciseRepository,
                "死虫式", "核心", "自重", "初级", "腹横肌", "腹直肌,竖脊肌", false, 5, "新手,进阶,高级",
                "康复级核心训练，安全且高效。",
                "仰卧，四肢朝天\n对侧手脚同时缓慢下放\n保持腰椎贴地\n交替进行",
                "腰始终贴地\n动作越慢越好\n配合呼吸节奏");

            // ---- 肩部扩展 ----
            Exercise facePull = createExercise(exerciseRepository,
                "绳索面拉", "肩部", "龙门架", "中级", "三角肌后束", "菱形肌,肩袖肌群", false, 7, "新手,进阶,高级",
                "改善肩部健康和体态的重要动作。",
                "将滑轮调至面部高度\n双手抓握绳索\n向面部拉动绳索\n顶峰收缩1-2秒后还原",
                "肘部向外打开\n感受三角肌后束发力");

            Exercise arnoldPress = createExercise(exerciseRepository,
                "阿诺德推举", "肩部", "哑铃", "中级", "三角肌前束", "三角肌中束,肱三头肌", true, 7, "进阶,高级",
                "施瓦辛格经典动作，全程刺激前中束。",
                "坐姿，双手持哑铃于胸前（掌心向内）\n推起同时外旋手腕至掌心向前\n推至头顶上方\n控制还原",
                "旋转时肘部保持向外\n感受肩部全程张力");

            // ---- 胸部扩展 ----
            Exercise declinePushUp = createExercise(exerciseRepository,
                "下斜俯卧撑", "胸部", "自重", "中级", "胸大肌下部", "三角肌前束,肱三头肌", true, 5, "进阶,高级",
                "针对下胸的自重训练动作。",
                "双脚置于凳面，双手撑地\n身体呈直线\n屈肘下放至胸接近地面\n发力推起",
                "核心收紧不塌腰\n肘部与躯干约45°");

            // ---- 小腿补充 ----
            Exercise seatedCalfRaise = createExercise(exerciseRepository,
                "坐姿提踵", "腿部", "杠铃", "中级", "比目鱼肌", "腓肠肌", false, 5, "进阶,高级",
                "侧重比目鱼肌的小腿训练。",
                "坐于凳，前脚掌踩踏板\n杠铃或重物置于膝上\n踮脚至最高点\n控制下放",
                "膝盖角度固定\n感受比目鱼肌收缩");

            // ========== 训练计划 ==========
            TrainingPlan plan = new TrainingPlan();
            plan.setTitle("四分化力量训练");
            plan.setSubtitle("推-拉-腿-肩臂 循环推进");
            plan.setTargetType("增肌");
            plan.setDifficulty("进阶");
            plan.setDurationWeeks(8);
            plan.setDaysPerWeek(4);
            plan.setSummary("围绕推拉腿肩臂循环，每周4练，均衡发展全身肌群。");
            plan.setCustomPlan(false);
            plan = trainingPlanRepository.save(plan);

            TrainingPlanDay day1 = new TrainingPlanDay();
            day1.setPlanId(plan.getId());
            day1.setDayNo(1);
            day1.setTitle("推日");
            day1.setFocus("胸部 + 肩部前中束 + 肱三头肌");
            day1 = trainingPlanDayRepository.save(day1);
            createPlanItem(trainingPlanItemRepository, day1.getId(), bench.getId(), bench.getName(), 5, "8-10", 90, "渐进超负荷", 1);
            createPlanItem(trainingPlanItemRepository, day1.getId(), incDumbbell.getId(), incDumbbell.getName(), 4, "10-12", 75, "中等重量", 2);
            createPlanItem(trainingPlanItemRepository, day1.getId(), cableFly.getId(), cableFly.getName(), 3, "12-15", 60, "轻重量", 3);

            TrainingPlanDay day2 = new TrainingPlanDay();
            day2.setPlanId(plan.getId());
            day2.setDayNo(2);
            day2.setTitle("拉日");
            day2.setFocus("背部 + 肱二头肌");
            day2 = trainingPlanDayRepository.save(day2);
            createPlanItem(trainingPlanItemRepository, day2.getId(), pullUp.getId(), pullUp.getName(), 4, "8-10", 60, "自重或负重", 1);
            createPlanItem(trainingPlanItemRepository, day2.getId(), bbRow.getId(), bbRow.getName(), 4, "8-10", 90, "渐进超负荷", 2);
            createPlanItem(trainingPlanItemRepository, day2.getId(), seatedRow.getId(), seatedRow.getName(), 3, "10-12", 60, "中等重量", 3);

            TrainingPlanDay day3 = new TrainingPlanDay();
            day3.setPlanId(plan.getId());
            day3.setDayNo(3);
            day3.setTitle("腿日");
            day3.setFocus("股四头肌 + 腘绳肌 + 臀大肌");
            day3 = trainingPlanDayRepository.save(day3);
            createPlanItem(trainingPlanItemRepository, day3.getId(), squat.getId(), squat.getName(), 5, "5-8", 150, "力量优先", 1);
            createPlanItem(trainingPlanItemRepository, day3.getId(), rdl.getId(), rdl.getName(), 4, "8-10", 90, "中等重量", 2);
            createPlanItem(trainingPlanItemRepository, day3.getId(), lunge.getId(), lunge.getName(), 3, "10-12", 60, "中等重量", 3);

            TrainingPlanDay day4 = new TrainingPlanDay();
            day4.setPlanId(plan.getId());
            day4.setDayNo(4);
            day4.setTitle("肩臂日");
            day4.setFocus("三角肌 + 肱二头肌 + 肱三头肌");
            day4 = trainingPlanDayRepository.save(day4);
            createPlanItem(trainingPlanItemRepository, day4.getId(), ohp.getId(), ohp.getName(), 4, "8-10", 90, "中等重量", 1);
            createPlanItem(trainingPlanItemRepository, day4.getId(), lateralRaise.getId(), lateralRaise.getName(), 4, "12-15", 45, "轻重量", 2);
            createPlanItem(trainingPlanItemRepository, day4.getId(), barbellCurl.getId(), barbellCurl.getName(), 3, "10-12", 60, "中等重量", 3);

            // ========== 训练记录 ==========
            WorkoutSession session = new WorkoutSession();
            session.setUserId(user.getId());
            session.setTitle("推日训练");
            session.setFocus("卧推峰值输出");
            session.setSessionDate(LocalDate.now().minusDays(1));
            session.setStartedAt(LocalDateTime.now().minusDays(1).minusMinutes(68));
            session.setFinishedAt(LocalDateTime.now().minusDays(1));
            session.setDurationMinutes(68);
            session.setTotalVolume(new BigDecimal("24500"));
            session.setCalories(842);
            session.setFeelingScore(5);
            session.setNotes("卧推节奏和锁定都很稳定。");
            session.setCompletionStatus("COMPLETED");
            session = workoutSessionRepository.save(session);

            createWorkoutSet(workoutSetRepository, session.getId(), user.getId(), bench.getId(), bench.getName(), 1, "80", 10, 2, false);
            createWorkoutSet(workoutSetRepository, session.getId(), user.getId(), bench.getId(), bench.getName(), 2, "85", 8, 1, false);
            createWorkoutSet(workoutSetRepository, session.getId(), user.getId(), bench.getId(), bench.getName(), 3, "90", 6, 0, true);
            createWorkoutSet(workoutSetRepository, session.getId(), user.getId(), incDumbbell.getId(), incDumbbell.getName(), 1, "30", 10, 2, false);

            createMetric(bodyMetricRepository, user.getId(), LocalDate.now().minusDays(21), "77.8", "17.2", "31.4");
            createMetric(bodyMetricRepository, user.getId(), LocalDate.now().minusDays(14), "77.1", "16.9", "31.8");
            createMetric(bodyMetricRepository, user.getId(), LocalDate.now().minusDays(7), "76.8", "16.7", "32.1");
            createMetric(bodyMetricRepository, user.getId(), LocalDate.now(), "76.5", "16.5", "32.4");

            createPersonalRecord(personalRecordRepository, user.getId(), bench.getId(), bench.getName(), "MAX_WEIGHT", "90", session.getId());

            CommunityPost post = new CommunityPost();
            post.setUserId(user.getId());
            post.setAuthorName(user.getNickname());
            post.setAuthorAvatar(user.getAvatarUrl());
            post.setContent("今天卧推刷新PR！90kg完成6次，下一次冲击更稳定的5x5。");
            post.setPostType("PR");
            post.setTopicTags("打卡,卧推,PR");
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

            createComment(communityCommentRepository, post.getId(), friend.getId(), friend.getNickname(), "卧推动作质量很高，控制力越来越好！");
            createLike(communityLikeRepository, post.getId(), friend.getId());

            post.setCommentCount((int) communityCommentRepository.findByPostIdOrderByCreatedAtAsc(post.getId()).size());
            post.setLikeCount((int) communityLikeRepository.countByPostId(post.getId()));
            communityPostRepository.save(post);
        };
    }

    private Exercise createExercise(ExerciseRepository repository,
                                    String name, String category, String equipment,
                                    String difficulty, String primaryMuscle,
                                    String secondaryMuscles, boolean isCompound,
                                    int priority, String suitableLevel,
                                    String description, String steps, String tips) {
        Exercise e = new Exercise();
        e.setName(name);
        e.setCategory(category);
        e.setEquipment(equipment);
        e.setDifficulty(difficulty);
        e.setPrimaryMuscle(primaryMuscle);
        e.setSecondaryMuscles(secondaryMuscles);
        e.setCompound(isCompound);
        e.setPriority(priority);
        e.setSuitableLevel(suitableLevel);
        e.setDescription(description);
        e.setMovementSteps(steps);
        e.setTips(tips);
        e.setCoverImage("");
        return repository.save(e);
    }

    private void createPlanItem(TrainingPlanItemRepository repo, Long dayId,
                                Long exerciseId, String name, int sets,
                                String reps, int rest, String weightMode, int sortNo) {
        TrainingPlanItem item = new TrainingPlanItem();
        item.setDayId(dayId);
        item.setExerciseId(exerciseId);
        item.setExerciseName(name);
        item.setSetsCount(sets);
        item.setReps(reps);
        item.setRestSeconds(rest);
        item.setWeightMode(weightMode);
        item.setSortNo(sortNo);
        repo.save(item);
    }

    private void createWorkoutSet(WorkoutSetRepository repo, Long sessionId,
                                  Long userId, Long exerciseId, String name,
                                  int setNo, String weight, int reps, int rir, boolean isPr) {
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
        repo.save(set);
    }

    private void createMetric(BodyMetricRepository repo, Long userId,
                              LocalDate date, String weight, String bodyFat, String muscle) {
        BodyMetric m = new BodyMetric();
        m.setUserId(userId);
        m.setMetricDate(date);
        m.setWeightKg(new BigDecimal(weight));
        m.setBodyFatRate(new BigDecimal(bodyFat));
        m.setSkeletalMuscleKg(new BigDecimal(muscle));
        repo.save(m);
    }

    private void createPersonalRecord(PersonalRecordRepository repo, Long userId,
                                      Long exId, String exName, String type,
                                      String value, Long sessionId) {
        PersonalRecord pr = new PersonalRecord();
        pr.setUserId(userId);
        pr.setExerciseId(exId);
        pr.setExerciseName(exName);
        pr.setRecordType(type);
        pr.setRecordValue(new BigDecimal(value));
        pr.setAchievedAt(LocalDateTime.now().minusDays(1));
        pr.setSourceSessionId(sessionId);
        repo.save(pr);
    }

    private void createComment(CommunityCommentRepository repo, Long postId,
                               Long userId, String authorName, String content) {
        CommunityComment c = new CommunityComment();
        c.setPostId(postId);
        c.setUserId(userId);
        c.setAuthorName(authorName);
        c.setContent(content);
        repo.save(c);
    }

    private void createLike(CommunityLikeRepository repo, Long postId, Long userId) {
        CommunityLike like = new CommunityLike();
        like.setPostId(postId);
        like.setUserId(userId);
        repo.save(like);
    }
}
