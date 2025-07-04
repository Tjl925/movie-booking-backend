package com.example.movie_booking_backend.common.utils;

import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailApi {
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from ;// 发件人
    public EmailApi(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * 发送html的邮件
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     * @return 是否成功
     */
    @SneakyThrows(Exception.class)
    public boolean sendHtmlEmail(String subject, String content, String... to){
        // 创建邮件消息
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setFrom(from);
        // 设置收件人
        helper.setTo(to);
        // 设置邮件主题
        helper.setSubject(subject);
        // 设置邮件内容
        helper.setText(content, true);

        // 发送邮件
        mailSender.send(mimeMessage);

//        log.info("发送邮件成功");
        return true;

    }

    /**
     * 发送验证码邮件
     * @param to 收件人邮箱
     * @param verificationCode 验证码
     * @return 是否发送成功
     */
    public boolean sendVerificationCodeEmail(String to, String verificationCode) {
        try {
            String subject = "您的验证码";
            String content = "欢迎注册凶凶凶影院,您的验证码是: " + verificationCode + "，该验证码5分钟内有效。";
            System.out.println("开始发送验证码邮件到: " + to);
            
            // 创建邮件消息 - 直接使用SimpleMailMessage而不是调用sendGeneralEmail以便更好地控制异常
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            // 发送邮件
            mailSender.send(message);
            
            System.out.println("验证码邮件发送成功: " + to);
            return true;
        } catch (Exception e) {
            System.err.println("发送验证码邮件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 发送密码邮件
     * @param passWord 密码
     * @param to 收件人邮箱
     * @return 是否发送成功
     */
    public Boolean sendPassWordEmail(String passWord, String to) {
        try {
            String subject = "您的密码";
            String content = "您的密码是: " + passWord;
            System.out.println("开始发送密码邮件到: " + to);
            
            // 创建邮件消息 - 直接使用SimpleMailMessage而不是调用sendGeneralEmail以便更好地控制异常
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            // 发送邮件
            mailSender.send(message);
            
            System.out.println("密码邮件发送成功: " + to);
            return true;
        } catch (Exception e) {
            System.err.println("发送密码邮件失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
