package com.thamer.Rent_System.controller.view;

import com.thamer.Rent_System.model.UserEntity;
import com.thamer.Rent_System.service.MailService;
import com.thamer.Rent_System.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RegistrationController {

    private final UserService userService;
    private final MailService mailService;

    public RegistrationController(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
    }

    // ==========================================
    // 1. تسجيل مستخدم جديد
    // ==========================================
    @GetMapping("/UserRegister")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserEntity());
        return "UserRegister";
    }

    @PostMapping("/UserRegister")
    public String registerUser(@ModelAttribute("user") UserEntity user, Model model) {
        try {
            userService.registerNewUser(user);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", "فشل التسجيل: " + e.getMessage());
            return "UserRegister";
        }
    }

    // ==========================================
    // 2. طلب استعادة كلمة المرور (إرسال الكود)
    // ==========================================
    @GetMapping("/forgot-password")
    public String showForgetPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        try {
            // توليد الكود وحفظه في خانة التوكن للمستخدم
            String code = userService.createResetPasswordCode(email);
            // إرسال الإيميل (تأكد أنها @Async لسرعة الرد)
            mailService.sendResetPasswordEmail(email, code);

            // نرجع لنفس الصفحة مع باراميتر النجاح والإيميل لفتح المودال
            return "redirect:/forgot-password?success&email=" + email;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "عذراً: " + e.getMessage());
            return "redirect:/forgot-password";
        }
    }

    // ==========================================
    // 3. التحقق من كود الـ OTP
    // ==========================================
    @PostMapping("/verify-code")
    public String processVerifyCode(@RequestParam("email") String email,
            @RequestParam("code") String code,
            RedirectAttributes redirectAttributes) {

        // البحث عن المستخدم باستخدام الكود (التوكن)
        UserEntity user = userService.getByResetPasswordToken(code);

        if (user != null && user.getEmail().equalsIgnoreCase(email)) {
            // نجاح: نذهب لصفحة تعيين كلمة المرور الجديدة ونمرر الكود كـ token
            return "redirect:/reset-password?token=" + code;
        } else {
            // فشل: نعود للمودال ونظهر رسالة الخطأ
            redirectAttributes.addFlashAttribute("otpError", "رمز التحقق غير صحيح أو منتهي الصلاحية!");
            return "redirect:/forgot-password?success&email=" + email;
        }
    }

    // ==========================================
    // 4. عرض صفحة تغيير كلمة المرور الجديدة
    // ==========================================
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        UserEntity user = userService.getByResetPasswordToken(token);

        if (user == null) {
            model.addAttribute("error", "رابط الاستعادة منتهي أو غير صالح.");
            return "forget-password";
        }

        model.addAttribute("token", token);
        return "reset-password";
    }

    // ==========================================
    // 5. حفظ كلمة المرور الجديدة نهائياً
    // ==========================================
    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam("token") String token,
            @RequestParam("password") String password,
            RedirectAttributes redirectAttributes) {

        // طباعة للتحقق (تظهر في الكونسول عندك)
        System.out.println("محاولة تغيير كلمة المرور للتوكن: " + token);

        UserEntity user = userService.getByResetPasswordToken(token);

        if (user == null) {
            System.out.println("فشل: لم يتم العثور على مستخدم بهذا التوكن!");
            redirectAttributes.addFlashAttribute("error", "حدث خطأ: الرابط منتهي أو غير صالح، حاول مجدداً.");
            return "redirect:/forgot-password";
        }

        // تحديث كلمة المرور
        userService.updatePassword(user, password);
        System.out.println("نجاح: تم تغيير كلمة المرور للمستخدم: " + user.getUsername());

        redirectAttributes.addFlashAttribute("message", "تم تغيير كلمة المرور بنجاح! يمكنك الآن تسجيل الدخول.");
        return "redirect:/login";
    }
}