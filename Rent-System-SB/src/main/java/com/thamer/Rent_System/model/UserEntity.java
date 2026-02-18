package com.thamer.Rent_System.model;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Collection;
import java.util.Collections;

@Entity
@Table(name = "users")
@Data
@Builder // لسهولة إنشاء الكائنات
@NoArgsConstructor // مطلوب للـ JPA
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role;

    @Column(nullable = false)
    private boolean enabled = false;

    // ---------------------------------------------------
    // دوال UserDetails المطلوبة من Spring Security
    // ---------------------------------------------------

    // داخل UserEntity.java
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // نرجع الرول كما هي لأننا خزنها بصيغة ROLE_ADMIN
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    // هذه الدالة تربط حقل الـ enabled الخاص بك بمنطق السبرنق
    // إذا رجعت false، السبرنق سيرفض تسجيل الدخول ويقول "Account Disabled"
    public boolean isEnabled() {
        return enabled;
    }

    // الخصائص التالية يمكن تركها true دائمًا إلا إذا كنت تريد تعقيداً أكثر
    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Column(name = "reset_password_token")
    private String resetPasswordToken;

    // Getter and Setter
    public String getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(String resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }
}
