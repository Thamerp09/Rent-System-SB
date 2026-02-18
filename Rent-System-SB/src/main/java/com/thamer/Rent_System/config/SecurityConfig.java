package com.thamer.Rent_System.config;

import com.thamer.Rent_System.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. مهم جداً: تعطيل CSRF لروابط الـ API عشان طلبات الـ POST تشتغل من الجافا
                // سكريبت
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .authorizeHttpRequests(authorize -> authorize

                        // 2. السماح للروت "/" عشان التوجيه يشتغل
                        .requestMatchers("/").permitAll()

                        // 3. السماح لرابط الشات (/api/**)
                        .requestMatchers("/api/**").permitAll()
                        // السماح للملفات الثابتة
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/manifest.json", "/service-worker.js")
                        .permitAll()

                        // ✅ تمت إضافة /verify-code هنا لكي يعمل رمز التحقق بدون تسجيل دخول
                        .requestMatchers("/register", "/UserRegister", "/forgot-password", "/reset-password",
                                "/verify-code", "/error")
                        .permitAll()

                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // أي طلب آخر يتطلب دخول
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/home", true)
                        .permitAll())
                .logout(logout -> logout
                        .permitAll()
                        .logoutSuccessUrl("/"));

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}