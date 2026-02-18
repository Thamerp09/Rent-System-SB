package com.thamer.Rent_System.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.thamer.Rent_System.model.*;


@Repository  // اختياري لأن JpaRepository بيضيفها ضمنيًا
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    // هنا تقدر تضيف دوال استعلام مخصصة إذا تبغى
}
