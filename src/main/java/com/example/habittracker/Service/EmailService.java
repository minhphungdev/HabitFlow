package com.example.habittracker.Service;

import com.example.habittracker.Domain.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    @Value("phungnhatminh2003@gmail.com")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender1) {
        this.mailSender = mailSender1;
    }

    public void sendWelcomeEmail(User newUser) {
        String recipientEmail = newUser.getEmail();
        String subject = "Chào mừng bạn đến với HabitFlow - Hành trình rèn luyện thói quen của bạn!";

        StringBuilder body = new StringBuilder();
        body.append("Chào ").append(newUser.getUserName()).append(",\n\n");
        body.append("Chào mừng bạn đã gia nhập cộng đồng HabitFlow! Chúng tôi rất vui mừng khi bạn quyết định cùng chúng tôi xây dựng những thói quen tốt và chinh phục các thử thách.\n\n");
        body.append("Tại HabitFlow, bạn có thể:\n");
        body.append("- Tạo và theo dõi các nhiệm vụ hàng ngày.\n");
        body.append("- Xây dựng và duy trì những thói quen tích cực hoặc từ bỏ những thói quen tiêu cực.\n");
        body.append("- Quản lý các công việc cần làm.\n");
        body.append("- Tham gia vào các thử thách thú vị và kết nối với cộng đồng.\n");
        body.append("- Ghi lại hành trình của bạn với nhật ký cá nhân.\n\n");
        body.append("Hãy bắt đầu hành trình của bạn ngay hôm nay bằng cách tạo thử thách đầu tiên hoặc thêm một thói quen mới.\n");
        body.append("Nếu có bất kỳ câu hỏi nào, đừng ngần ngại liên hệ với chúng tôi nhé!\n\n");
        body.append("Chúc bạn có những trải nghiệm tuyệt vời với HabitFlow!\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ HabitFlow\n");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText(body.toString());

        mailSender.send(message);
    }

    public void sendEmailCompleteChallenge(UserChallenge userChallenge) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String recipientEmail = userChallenge.getUser().getEmail();
        String emailSubject = "Chúc mừng bạn đã hoàn thành thử thách: " + userChallenge.getChallenge().getTitle();

        StringBuilder body = new StringBuilder();
        body.append("Chào ").append(userChallenge.getUser() != null ? userChallenge.getUser().getUserName() : "").append(",\n\n");
        body.append("Chúng tôi xin chúc mừng bạn đã hoàn thành thử thách \"").append(userChallenge.getChallenge().getTitle()).append("\" một cách xuất sắc!\n");
        body.append("Bạn đã trải qua một hành trình đáng nhớ từ ngày ").append(userChallenge.getStartDate().format(dateFormatter)).append(" đến ngày ").append(userChallenge.getEndDate().format(dateFormatter)).append(".\n\n");
        body.append("Trong suốt thử thách, bạn đã đạt được những kết quả ấn tượng:\n");
        body.append("- Tổng số lần thực hiện thói quen thành công: ").append(userChallenge.getTotalCompletedTasks()).append("\n");
        body.append("- Chuỗi thực hiện tốt nhất của bạn: ").append(userChallenge.getBestStreak()).append(" ngày liên tiếp\n");
        body.append("- Tổng số lần bỏ lỡ thói quen: ").append(userChallenge.getSkippedTasks()).append("\n\n");
        body.append("Bạn nhận được \"").append(userChallenge.getCoinEarn()).append("\" xu khi hoàn thành thử thách!\n");
        body.append("Đánh giá tổng thể sự tiến bộ: ").append(userChallenge.getEvaluateProgress()).append("\n");
        body.append("Đây là một thành tựu đáng tự hào! Bạn có thể chia sẻ hành trình và kết quả này với bạn bè và cộng đồng.\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ ứng dụng của bạn\n");
        body.append("HabitFlow");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject(emailSubject);
        message.setText(body.toString());

        mailSender.send(message);
    }

    public void sendStreakLostNotification(UserChallenge userChallenge, long currentStreak) {
        String subject = "Thông báo: Chuỗi của thử thách \"" + userChallenge.getChallenge().getTitle() + "\" của bạn đã bị mất!";

        StringBuilder body = new StringBuilder();
        body.append("Chào bạn ").append(userChallenge.getUser().getUserName()).append(",\n\n");
        body.append("Chúng tôi rất tiếc phải thông báo rằng chuỗi liên tiếp thực hiện thử thách \"").append(userChallenge.getChallenge().getTitle()).append("\" của bạn đã bị mất.\n\n");
        body.append("Bạn đã có một chuỗi ấn tượng là ").append(currentStreak).append(" ngày liên tiếp!\n");
        body.append("Đừng nản lòng nhé! Hãy bắt đầu lại từ hôm nay và xây dựng một chuỗi mới.\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ ứng dụng của bạn\n");
        body.append("HabitFlow");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(userChallenge.getUser().getEmail());
        message.setSubject(subject);
        message.setText(body.toString());

        mailSender.send(message);
    }

    public void sendEmailTaskUnComplete(List<UserDaily> userDailyUnCompleteList, List<UserHabit> userHabitUnCompleteList, User user) {
        if (userDailyUnCompleteList.isEmpty() && userHabitUnCompleteList.isEmpty()) {
            return;
        }
        String recipientEmail = user.getEmail();
        String userName = user.getUserName();

        // Kiểm tra nếu người dùng chưa đăng nhập hôm nay
//    LocalDateTime todayStart = LocalDate.now().atStartOfDay();
//    if (user.getLastLogin() != null && user.getLastLogin().isAfter(todayStart)) {
//        return;
//    }

        String subject = "Thông báo: Bạn có các thói quen chưa hoàn thành!";
        StringBuilder body = new StringBuilder();
        body.append("Chào bạn ").append(userName).append(",\n\n");
        body.append("Hôm nay sắp kết thúc, nhưng bạn vẫn chưa hoàn thành một số thói quen \n\n");
        body.append("Danh sách các nhiệm vụ chưa hoàn thành:\n");

        if (!userDailyUnCompleteList.isEmpty()) {
            body.append("- Thói quen hàng ngày:\n");
            for (UserDaily userDaily : userDailyUnCompleteList) {
                body.append("  + ").append(userDaily.getDaily().getTitle()).append("\n");
            }
        }

        if (!userHabitUnCompleteList.isEmpty()) {
            body.append("- Thói quen:\n");
            for (UserHabit userHabit : userHabitUnCompleteList) {
                body.append("  + ").append(userHabit.getHabit().getTitle()).append("\n");
            }
        }

        body.append("\nHãy đăng nhập ngay để hoàn thành các nhiệm vụ này trước khi ngày kết thúc nhé!\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ ứng dụng của bạn\n");
        body.append("HabitFlow");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText(body.toString());

        mailSender.send(message);
    }

    @Async
    public void sendEmailReceiveAchievement(UserAchievement userAchievement) {
        String recipientEmail = userAchievement.getUser().getEmail();

        String subject = "Bạn đã nhận được Thành tựu mới!";
        StringBuilder body = new StringBuilder();
        body.append("Chào ").append(userAchievement.getUser().getUserName()).append(",\n\n");
        body.append("Tuyệt vời!\n");
        body.append("Chúng tôi rất vui mừng thông báo rằng bạn vừa mở khóa một thành tựu mới: **\"").append(userAchievement.getAchievement().getTitle()).append("\"**.\n\n");

        // if (achievement.getDescription() != null && !achievement.getDescription().isEmpty()) {
        //     body.append(achievement.getDescription()).append("\n\n");
        // }

        body.append("Để vinh danh thành tích xuất sắc này, bạn nhận được:\n");
        if (userAchievement.getAchievement().getChallengeBonus() != null && userAchievement.getAchievement().getChallengeBonus() > 0) {
            body.append("- Thêm ").append(userAchievement.getAchievement().getChallengeBonus()).append(" lượt tạo thử thách mới (challenge limit).\n");
        }
        if (userAchievement.getAchievement().getTaskBonus() != null && userAchievement.getAchievement().getTaskBonus() > 0) {
            body.append("- Thêm ").append(userAchievement.getAchievement().getTaskBonus()).append(" lượt tạo nhiệm vụ mới (task limit).\n");
        }
        if (userAchievement.getAchievement().getCoinBonus() != null && userAchievement.getAchievement().getCoinBonus() > 0) {
            body.append("- ").append(userAchievement.getAchievement().getCoinBonus()).append(" xu vào tài khoản của bạn.\n");
        }
        body.append("\n");
        body.append("Hãy tiếp tục hành trình rèn luyện thói quen và chinh phục những đỉnh cao mới nhé!\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ ứng dụng HabitFlow");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(recipientEmail);
        message.setSubject(subject);
        message.setText(body.toString());

        mailSender.send(message);
    }

    public void sendStreakProtectNotification(UserChallenge userChallenge, long currentStreak, Integer streakProtectionCount) {
        String subject = "Thông báo: Chuỗi của thử thách \"" + userChallenge.getChallenge().getTitle() + "\" của bạn đã được bảo vệ!";

        StringBuilder body = new StringBuilder();
        body.append("Chào bạn ").append(userChallenge.getUser().getUserName()).append(",\n\n");
        body.append("Vật phẩm 'Bảo vệ chuỗi' đã được sử dụng thành công cho thử thách \"").append(userChallenge.getChallenge().getTitle()).append("\".\n\n");
        body.append("Chuỗi ấn tượng dài ").append(currentStreak).append(" ngày của bạn đã được an toàn.\n");
        body.append("Bạn còn lại ").append(streakProtectionCount).append(" lượt bảo vệ.\n\n");
        body.append("Hãy tiếp tục duy trì thói quen và chinh phục thử thách nhé!\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ ứng dụng của bạn\n");
        body.append("HabitFlow");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(userChallenge.getUser().getEmail());
        message.setSubject(subject);
        message.setText(body.toString());

        mailSender.send(message);
    }

    public void sendReminderLogin(User user, List<UserChallenge> userChallenges, long between) {
        String subject = "HabitFlow nhớ bạn! Hãy quay lại và tiếp tục hành trình của mình!";

        StringBuilder body = new StringBuilder();
        body.append("Chào bạn ").append(user.getUserName()).append(",\n\n");
        body.append("Chúng tôi nhận thấy đã ").append(between).append(" ngày kể từ lần cuối bạn đăng nhập vào HabitFlow. ");
        body.append("Chúng tôi nhớ bạn và mong bạn sẽ sớm quay trở lại.\n\n");

        if (userChallenges != null && !userChallenges.isEmpty()) {
            body.append("Bạn vẫn đang tham gia các thử thách tuyệt vời này:\n");
            for (UserChallenge uc : userChallenges) {
                body.append("- \"").append(uc.getChallenge().getTitle()).append("\"\n");
            }
            body.append("\n");
        }

        body.append("Mỗi ngày là một cơ hội mới để xây dựng thói quen tốt và trở thành phiên bản tốt hơn của chính mình.\n");
        body.append("Đừng để những nỗ lực đã qua bị lãng quên. Hãy đăng nhập ngay hôm nay để tiếp tục hành trình và chinh phục các mục tiêu của bạn!\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ ứng dụng HabitFlow");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body.toString());

        mailSender.send(message);
    }

    @Async
    public void sendChallengeApprovedEmail(User recipient, Challenge challenge) {
        String subject = "Chúc mừng! Thử thách \"" + challenge.getTitle() + "\" của bạn đã được duyệt!";
        StringBuilder body = new StringBuilder();
        body.append("Chào ").append(recipient.getUserName()).append(",\n\n");
        body.append("Chúng tôi rất vui mừng thông báo rằng thử thách cộng đồng của bạn, \"").append(challenge.getTitle()).append("\", đã được quản trị viên duyệt.\n\n");
        body.append("Giờ đây, thử thách của bạn đã công khai và mọi người có thể tham gia cùng bạn.\n");
        body.append("Hãy chuẩn bị cho một hành trình đầy thú vị và truyền cảm hứng cho cộng đồng nhé!\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ HabitFlow");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(recipient.getEmail());
        message.setSubject(subject);
        message.setText(body.toString());

        mailSender.send(message);
    }

    @Async
    public void sendChallengeRejectedEmail(User recipient, Challenge challenge) {
        String subject = "Thông báo: Thử thách \"" + challenge.getTitle() + "\" của bạn không được duyệt.";
        StringBuilder body = new StringBuilder();
        body.append("Chào ").append(recipient.getUserName()).append(",\n\n");
        body.append("Chúng tôi xin thông báo rằng thử thách cộng đồng của bạn, \"").append(challenge.getTitle()).append("\", đã không được quản trị viên duyệt.\n\n");
        body.append("Có thể có một số lý do khiến thử thách của bạn không được duyệt, chẳng hạn như nội dung không phù hợp hoặc vi phạm các nguyên tắc cộng đồng.\n");
        body.append("Bạn có thể chỉnh sửa lại thử thách và gửi lại để được xem xét.\n\n");
        body.append("Nếu bạn có bất kỳ câu hỏi nào, vui lòng liên hệ với chúng tôi để được hỗ trợ.\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ HabitFlow");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(recipient.getEmail());
        message.setSubject(subject);
        message.setText(body.toString());

        mailSender.send(message);
    }

    public void sendPasswordResetEmail(User user, String token) {
        String subject = "Đặt lại mật khẩu cho tài khoản HabitFlow của bạn";
        String resetUrl = "http://localhost:8080/reset-password?token=" + token;

        StringBuilder body = new StringBuilder();
        body.append("Chào ").append(user.getUserName()).append(",\n\n");
        body.append("Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản HabitFlow của bạn.\n\n");
        body.append("Vui lòng nhấp vào liên kết dưới đây để đặt lại mật khẩu của bạn:\n");
        body.append(resetUrl).append("\n\n");
        body.append("Liên kết này sẽ hết hạn sau 30 phút. Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n");
        body.append("Trân trọng,\n");
        body.append("Đội ngũ HabitFlow\n");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(user.getEmail());
        message.setSubject(subject);
        message.setText(body.toString());

        mailSender.send(message);
    }
}
