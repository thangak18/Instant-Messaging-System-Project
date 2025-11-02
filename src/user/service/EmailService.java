package user.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * Service gửi email thật qua Gmail SMTP
 * Sử dụng JavaMail API
 */
public class EmailService {
    
    private String smtpHost;
    private int smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private String fromEmail;
    private String fromName;
    
    public EmailService() {
        loadConfiguration();
    }
    
    /**
     * Đọc cấu hình email từ config.properties
     */
    private void loadConfiguration() {
        Properties props = new Properties();
        
        try (FileInputStream fis = new FileInputStream("release/config.properties")) {
            props.load(fis);
            
            this.smtpHost = props.getProperty("email.host", "smtp.gmail.com");
            this.smtpPort = Integer.parseInt(props.getProperty("email.port", "587"));
            this.smtpUsername = props.getProperty("email.username", "");
            this.smtpPassword = props.getProperty("email.password", "");
            this.fromEmail = props.getProperty("email.from", this.smtpUsername);
            this.fromName = props.getProperty("email.from.name", "InstantChat System");
            
            System.out.println("✅ Email configuration loaded");
            System.out.println("   SMTP Host: " + smtpHost + ":" + smtpPort);
            System.out.println("   Username: " + (smtpUsername.isEmpty() ? "(chưa cấu hình)" : smtpUsername));
            System.out.println("   From: " + fromName + " <" + fromEmail + ">");
            
        } catch (IOException | NumberFormatException e) {
            System.err.println("⚠️  Không đọc được email config, dùng giá trị mặc định");
            setDefaultConfiguration();
        }
    }
    
    /**
     * Cấu hình mặc định
     */
    private void setDefaultConfiguration() {
        this.smtpHost = "smtp.gmail.com";
        this.smtpPort = 587;
        this.smtpUsername = "";
        this.smtpPassword = "";
        this.fromEmail = "noreply@chatsystem.com";
        this.fromName = "InstantChat System";
    }
    
    /**
     * Gửi email reset password THẬT qua Gmail SMTP
     * 
     * @param toEmail Email người nhận
     * @param fullName Tên đầy đủ của user
     * @param temporaryPassword Mật khẩu tạm thời
     * @return true nếu gửi thành công
     */
    public boolean sendResetPasswordEmail(String toEmail, String fullName, String temporaryPassword) {
        // Kiểm tra cấu hình
        if (smtpUsername == null || smtpUsername.isEmpty() || smtpPassword == null || smtpPassword.isEmpty()) {
            System.err.println("❌ Email chưa được cấu hình!");
            System.err.println("   Vui lòng cập nhật email.username và email.password trong config.properties");
            return false;
        }
        
        try {
            // Cấu hình properties cho JavaMail
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", String.valueOf(smtpPort));
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            
            // Tạo session với authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUsername, smtpPassword);
                }
            });
            
            // Tạo message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("[InstantChat] Khôi Phục Mật Khẩu");
            
            // Nội dung HTML
            String htmlContent = 
                "<html>" +
                "<body style='font-family: Arial, sans-serif; padding: 20px; background-color: #f5f7fa;'>" +
                "  <div style='max-width: 600px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
                "    <div style='text-align: center; margin-bottom: 30px;'>" +
                "      <h1 style='color: #0084FF; margin: 0;'>💬 InstantChat</h1>" +
                "    </div>" +
                "    <h2 style='color: #333;'>Xin chào " + fullName + ",</h2>" +
                "    <p style='color: #666; line-height: 1.6;'>Chúng tôi đã nhận được yêu cầu khôi phục mật khẩu cho tài khoản của bạn.</p>" +
                "    <div style='background-color: #f0f8ff; border-left: 4px solid #0084FF; padding: 20px; margin: 20px 0;'>" +
                "      <p style='margin: 0 0 10px 0; color: #333; font-weight: bold;'>Mật khẩu tạm thời của bạn:</p>" +
                "      <h1 style='margin: 10px 0; color: #0084FF; font-size: 32px; letter-spacing: 2px; font-family: monospace;'>" + temporaryPassword + "</h1>" +
                "    </div>" +
                "    <div style='background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0;'>" +
                "      <p style='margin: 0 0 10px 0; color: #856404; font-weight: bold;'>⚠️ LƯU Ý BẢO MẬT:</p>" +
                "      <ul style='margin: 0; padding-left: 20px; color: #856404;'>" +
                "        <li>Vui lòng đổi mật khẩu ngay sau khi đăng nhập</li>" +
                "        <li>Không chia sẻ mật khẩu này với bất kỳ ai</li>" +
                "        <li>Nếu bạn không yêu cầu khôi phục, hãy liên hệ admin ngay</li>" +
                "      </ul>" +
                "    </div>" +
                "    <p style='color: #666; line-height: 1.6;'>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi!</p>" +
                "    <hr style='border: none; border-top: 1px solid #eee; margin: 30px 0;'>" +
                "    <p style='color: #999; font-size: 12px; text-align: center;'>Email này được gửi tự động, vui lòng không trả lời.<br>© 2025 InstantChat System. All rights reserved.</p>" +
                "  </div>" +
                "</body>" +
                "</html>";
            
            message.setContent(htmlContent, "text/html; charset=utf-8");
            
            // Gửi email
            System.out.println("\n📧 Đang gửi email đến: " + toEmail + "...");
            Transport.send(message);
            System.out.println("✅ Email đã được gửi thành công!");
            
            return true;
            
        } catch (Exception e) {
            System.err.println("❌ Lỗi khi gửi email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Test cấu hình email
     */
    public boolean testEmailConfiguration() {
        System.out.println("\n🧪 Testing Email Configuration...");
        System.out.println("   SMTP Host: " + smtpHost);
        System.out.println("   SMTP Port: " + smtpPort);
        System.out.println("   Username: " + (smtpUsername.isEmpty() ? "(chưa cấu hình)" : smtpUsername));
        System.out.println("   From Email: " + fromEmail);
        System.out.println("   From Name: " + fromName);
        
        if (smtpUsername.isEmpty()) {
            System.out.println("\n⚠️  Email chưa được cấu hình!");
            return false;
        }
        
        System.out.println("\n✅ Email configuration OK (DEMO MODE)");
        return true;
    }
}

