package com.thamer.Rent_System.controller.view;

import com.thamer.Rent_System.service.MonthlyReportScheduler;
import com.thamer.Rent_System.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin") // هذا يضيف /admin لكل الروابط
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final MonthlyReportScheduler monthlyReportScheduler;

    public AdminController(UserService userService, MonthlyReportScheduler monthlyReportScheduler) {
        this.userService = userService;
        this.monthlyReportScheduler = monthlyReportScheduler;
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("pendingUsers", userService.getPendingUsers());
        return "admin_dashboard";
    }

    // زر التفعيل
    @PostMapping("/approve-user") // الرابط النهائي: /admin/approve-user
    public String approveUser(@RequestParam("userId") Long userId) {
        userService.activateUser(userId); // تفعيل
        return "redirect:/home";
    }

    // زر الرفض والحذف (تم التصحيح هنا)
    @PostMapping("/reject-user") // ⚠️ حذفنا كلمة /admin/ من هنا
    public String rejectUser(@RequestParam("userId") Long userId) {
        userService.deleteUser(userId); // ✅ تأكد أنها تنادي deleteUser وليس activateUser
        return "redirect:/home";
    }

    // إضافة هذه الميثود لاستقبال الطلب من صفحة الهوم
    @PostMapping("/send-manual-report")
    public String sendManualReport(RedirectAttributes redirectAttributes) {
        try {
            // تشغيل ميثود إرسال التقرير
            monthlyReportScheduler.sendMonthlyFinancialReport();

            redirectAttributes.addFlashAttribute("reportMessage",
                    "تم إرسال التقرير المالي بنجاح إلى بريدك الإلكتروني.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("reportError", "حدث خطأ أثناء الإرسال: " + e.getMessage());
        }
        return "redirect:/home";
    }
}