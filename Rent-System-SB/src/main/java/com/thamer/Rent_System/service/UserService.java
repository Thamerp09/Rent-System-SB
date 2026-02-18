package com.thamer.Rent_System.service;

import com.thamer.Rent_System.model.UserEntity;
import com.thamer.Rent_System.repository.UserRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // =========================================================================
    // 1. دالة تسجيل الدخول
    // =========================================================================
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. جلب المستخدم من الداتابيز
        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 2. بناء كائن السكيورتي
        return org.springframework.security.core.userdetails.User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())

                // ⚠️ التعديل الجوهري هنا:
                // نستخدم authorities() بدلاً من roles()
                // ونمرر لها الصلاحية القادمة من الداتابيز (التي تأكدنا أنها تبدأ بـ ROLE_)
                .authorities(userEntity.getAuthorities())

                .disabled(!userEntity.isEnabled())
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .build();
    }

    // =========================================================================
    // 2. دالة إنشاء حساب جديد (مع التحقق من الصلاحية)
    // =========================================================================
    @Transactional
    public void registerNewUser(UserEntity user) throws Exception {

        // 1. التحقق من تكرار البيانات
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new Exception("اسم المستخدم مسجل مسبقاً!");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new Exception("البريد الإلكتروني مسجل مسبقاً!");
        }

        // 2. ✅ التحقق من الصلاحية (ADMIN أو USER فقط)
        // إذا جاءت الصلاحية فارغة، نعتبرها USER افتراضياً
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("ROLE_USER");
        } else {
            // توحيد الصيغة (حذف المسافات وتحويل لحروف كبيرة)
            String inputRole = user.getRole().trim().toUpperCase();

            // تنظيف المدخل لو المستخدم كتب "ROLE_ADMIN" أو "ADMIN"
            inputRole = inputRole.replace("ROLE_", "");

            if (inputRole.equals("ADMIN")) {
                user.setRole("ROLE_ADMIN");
            } else if (inputRole.equals("USER")) {
                user.setRole("ROLE_USER");
            } else {
                // ❌ رمي خطأ إذا كانت الصلاحية غير ذلك
                throw new Exception("خطأ: نوع الحساب غير صحيح. يجب أن يكون ADMIN أو USER فقط.");
            }
        }

        // 3. تشفير كلمة المرور
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 4. تعطيل الحساب افتراضياً
        user.setEnabled(false);

        userRepository.save(user);
    }

    // =========================================================================
    // 3. دوال الأدمن
    // =========================================================================
    public List<UserEntity> getPendingUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isEnabled())
                .toList();
    }

    public long countPendingUsers() {
        return userRepository.findAll().stream()
                .filter(user -> !user.isEnabled())
                .count();
    }

    @Transactional
    public void activateUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("المستخدم غير موجود"));

        user.setEnabled(true);
        userRepository.save(user);
    }

    // في ملف UserService.java

    @Transactional
    public void deleteUser(Long userId) {
        // إذا كان الكود هنا activateUser فالنظام سيفعل المستخدم بدل حذفه!
        // تأكد أنها deleteById
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
        }
    }

    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    // 1. البحث عن المستخدم بواسطة الإيميل
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // 2. تحديث التوكن في قاعدة البيانات
    @Transactional
    public void updateResetPasswordToken(String token, String email) throws Exception {
        UserEntity user = userRepository.findByEmail(email);
        if (user != null) {
            user.setResetPasswordToken(token);
            userRepository.save(user);
        } else {
            throw new Exception("لم يتم العثور على مستخدم بهذا البريد: " + email);
        }
    }

    // 3. البحث عن المستخدم بواسطة التوكن (تستخدم عند الضغط على الرابط في الإيميل)
    public UserEntity getByResetPasswordToken(String token) {
        return userRepository.findByResetPasswordToken(token);
    }

    @Transactional
    public void updatePassword(UserEntity user, String newPassword) {
        // 1. تشفير كلمة المرور الجديدة
        user.setPassword(passwordEncoder.encode(newPassword));

        // 2. مسح التوكن لكي لا يستخدم مرة أخرى (أمان)
        user.setResetPasswordToken(null);

        // 3. حفظ في قاعدة البيانات
        userRepository.save(user);
    }

    @Transactional
    public String createResetPasswordCode(String email) throws Exception {
        UserEntity user = userRepository.findByEmail(email);
        if (user == null)
            throw new Exception("البريد غير موجود");

        // توليد رقم عشوائي من 6 أرقام
        String code = String.valueOf((int) ((Math.random() * 900000) + 100000));

        user.setResetPasswordToken(code); // سنخزن الكود في نفس خانة التوكن
        userRepository.save(user);
        return code;
    }
}