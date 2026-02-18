package com.thamer.Rent_System.repository;

import com.thamer.Rent_System.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    // نحتاج هذه الدالة للبحث عن المستخدم عند تسجيل الدخول
    Optional<UserEntity> findByUsername(String username);

    // للبحث عن طريق الايميل (للتأكد من عدم التكرار عند التسجيل)
    boolean existsByEmail(String email);

    // أضف هذه الأسطر:
    UserEntity findByEmail(String email);

    UserEntity findByResetPasswordToken(String token);

}