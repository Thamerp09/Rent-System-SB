package com.thamer.Rent_System.service;

import com.thamer.Rent_System.model.RentRecord;
import com.thamer.Rent_System.model.UserEntity;

import jakarta.transaction.Transactional;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class MonthlyReportScheduler {

        private final RentalManagementService rentalService;
        private final MailService mailService;
        private final UserService userService; // أضفنا خدمة المستخدمين هنا

        public MonthlyReportScheduler(RentalManagementService rentalService, MailService mailService,
                        UserService userService) {
                this.rentalService = rentalService;
                this.mailService = mailService;
                this.userService = userService;
        }

        @Scheduled(cron = "0 0 12 1 * *")
        @Transactional
        public void sendMonthlyFinancialReport() {
                System.out.println("DEBUG: [السكجولر] بدء عملية توليد التقرير المجمع...");

                try {
                        List<RentRecord> dueNow = rentalService.getDueNowPayments();
                        List<RentRecord> upcoming = rentalService.getUpcomingPayments();
                        String reportHtml = buildHtmlContent(dueNow, upcoming);

                        List<UserEntity> allUsers = userService.getAllUsers();

                        if (allUsers == null || allUsers.isEmpty()) {
                                System.out.println(
                                                "DEBUG: [السكجولر] تنبيه: لا يوجد مستخدمين في قاعدة البيانات للإرسال لهم!");
                                return;
                        }

                        System.out.println("DEBUG: [السكجولر] تم العثور على (" + allUsers.size()
                                        + ") مستخدمين. جاري بدء الإرسال...");

                        for (UserEntity user : allUsers) {
                                if (user.getEmail() != null && !user.getEmail().trim().isEmpty()) {
                                        mailService.sendHtmlEmail(user.getEmail(), "تقرير الإيجارات الشهري الموحد",
                                                        reportHtml);
                                } else {
                                        System.out.println("DEBUG: [السكجولر] تخطي المستخدم (" + user.getUsername()
                                                        + ") لعدم وجود إيميل.");
                                }
                        }
                        System.out.println("DEBUG: [السكجولر] انتهت حلقة الإرسال بنجاح.");

                } catch (Exception e) {
                        System.err.println("DEBUG: [السكجولر] خطأ كارثي أثناء التوليد: " + e.getMessage());
                        e.printStackTrace();
                }
        }

        // ميثود مساعدة لبناء محتوى الإيميل
        private String buildHtmlContent(List<RentRecord> dueNow, List<RentRecord> upcoming) {
                BigDecimal totalDue = dueNow.stream().map(RentRecord::getAmount).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                BigDecimal totalUpcoming = upcoming.stream().map(RentRecord::getAmount).reduce(BigDecimal.ZERO,
                                BigDecimal::add);

                StringBuilder html = new StringBuilder();
                html.append("<div dir='rtl' style='font-family: Arial, sans-serif; border: 2px solid #b49457; padding: 25px; border-radius: 20px; background-color: #fcfcfc;'>");
                html.append("<h2 style='color: #b49457; text-align: center;'>التقرير المالي الشهري - نظام الإيجارات</h2>");
                html.append("<hr style='border: 1px solid #eee;'>");

                // قسم المستحقات الحالية
                html.append("<h3 style='color: #d35400;'>⚠️ دفعات مستحقة الآن:</h3>");
                html.append("<p style='font-size: 16px;'>إجمالي المبالغ المتأخرة: <b style='color: red;'>")
                                .append(totalDue).append(" ريال</b></p>");

                if (dueNow.isEmpty()) {
                        html.append("<p style='color: green;'>لا توجد مستحقات متأخرة حالياً.</p>");
                } else {
                        html.append("<table border='1' style='width:100%; border-collapse: collapse; text-align: right;'>");
                        html.append("<tr style='background-color: #b49457; color: white;'><th>المستأجر</th><th>المبلغ</th><th>تاريخ الاستحقاق</th></tr>");
                        for (RentRecord r : dueNow) {
                                html.append("<tr><td style='padding: 8px;'>")
                                                .append(r.getContract().getTenant().getName()).append("</td>");
                                html.append("<td style='padding: 8px;'>").append(r.getAmount()).append("</td>");
                                html.append("<td style='padding: 8px;'>").append(r.getDueDate()).append("</td></tr>");
                        }
                        html.append("</table>");
                }

                // قسم الدفعات القادمة
                html.append("<h3 style='color: #2980b9; margin-top: 30px;'>📅 دفعات قادمة (خلال 60 يوم):</h3>");
                html.append("<p style='font-size: 16px;'>إجمالي المبالغ القادمة: <b>").append(totalUpcoming)
                                .append(" ريال</b></p>");

                html.append("<p style='color: #7f8c8d; font-size: 12px; margin-top: 30px; text-align: center;'>هذه رسالة تلقائية من النظام الموحد للإدارة.</p>");
                html.append("</div>");

                return html.toString();
        }
}