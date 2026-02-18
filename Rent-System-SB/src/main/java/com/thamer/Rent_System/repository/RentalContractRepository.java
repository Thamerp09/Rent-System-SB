package com.thamer.Rent_System.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.thamer.Rent_System.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Repository
public interface RentalContractRepository extends JpaRepository<RentalContract, Long> {

   List<RentalContract> findByTenant_Id(Long tenantId);

   RentalContract findByTenant(Tenant tenant);

   // وظيفتها: "ابحث عن كل العقود التي تطابق هذا الموقع"
   List<RentalContract> findByLocation(PropertyLocation location);

   long countByLocation(PropertyLocation location);

   // دالة مهمة: تحسب عدد العقود لمستأجر معين
   long countByTenantId(Long tenantId);

   // دالة جديدة: تبحث عن عقد وتعيد ID المستأجر المرتبط به (لزيادة الكفاءة)
   @Query("SELECT c.tenant.id FROM RentalContract c WHERE c.id = :contractId")
   Long findTenantIdByContractId(@Param("contractId") Long contractId);

}
