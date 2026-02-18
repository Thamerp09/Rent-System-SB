package com.thamer.Rent_System.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    // جلب القيم من ملف application.properties لسهولة التعديل
    @Value("${spring.mail.username}")
    private String fromEmail;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async // تجعل هذه الدالة تعمل في الخلفية فوراً دون انتظار
    public void sendResetPasswordEmail(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("رمز التحقق لإعادة تعيين كلمة المرور");

        message.setText("عزيزي المستخدم،\n\n" +
                "رمز التحقق الخاص بك هو: " + code + "\n\n" +
                "يرجى إدخال هذا الرمز في الموقع لإتمام عملية استعادة الحساب.\n" +
                "هذا الرمز صالح لفترة محدودة.");

        mailSender.send(message);
    }

    // أضف هذه الميثود داخل MailService.java
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            System.out.println("DEBUG: [خدمة البريد] جاري الإرسال إلى: " + toEmail);
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(
                    message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("DEBUG: [خدمة البريد] ✅ تم الإرسال بنجاح لـ: " + toEmail);
        } catch (Exception e) {
            System.err.println("DEBUG: [خدمة البريد] ❌ فشل الإرسال لـ " + toEmail + " بسبب: " + e.getMessage());
        }
    }

}