package com.thamer.Rent_System.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.thamer.Rent_System.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Repository
public interface RentRecordRepository extends JpaRepository<RentRecord, Long> {

    List<RentRecord> findByContractId(Long contractId);

    List<RentRecord> findByDueDateBetween(LocalDate start, LocalDate end);

    List<RentRecord> findByPaidFalse();

    RentRecord findTopByContractOrderByDueDateDesc(RentalContract contract);

    // ------الخاص بالملخص المالي------//

    // 1. التعديل الأول: أضفنا Query لجلب البيانات مرة واحدة لهذه الدالة
    @Query("SELECT r FROM RentRecord r " +
            "JOIN FETCH r.contract c " +
            "JOIN FETCH c.tenant t " +
            "WHERE r.paid = false AND r.dueDate <= :date")
    List<RentRecord> findByPaidIsFalseAndDueDateLessThanEqual(@Param("date") LocalDate date);

    // 2. التعديل الثاني: أضفنا Query لهذه الدالة أيضاً
    @Query("SELECT r FROM RentRecord r " +
            "JOIN FETCH r.contract c " +
            "JOIN FETCH c.tenant t " +
            "WHERE r.paid = false AND r.dueDate BETWEEN :startDate AND :endDate")
    List<RentRecord> findByPaidIsFalseAndDueDateBetween(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ---- حساب إجمالي مبلغ الدفعات (المدفوعة او الغير مدفوعة) لسنة معينة
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RentRecord r WHERE YEAR(r.dueDate) = :year AND r.paid = :isPaid")
    BigDecimal calculateTotalByYearAndPaidStatus(@Param("year") int year, @Param("isPaid") boolean isPaid);

    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RentRecord r WHERE YEAR(r.dueDate) = :year")
    BigDecimal calculateTotalByYear(@Param("year") int year);

    // حساب إجمالي المبالغ المحصلة لكل السنوات
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RentRecord r WHERE r.paid = true")
    BigDecimal calculateTotalCollectedOverall();

    // حساب إجمالي المبالغ المتوقعة لكل السنوات
    @Query("SELECT COALESCE(SUM(r.amount), 0) FROM RentRecord r")
    BigDecimal calculateTotalExpectedOverall();

    // هذه الدوال سليمة لأنها تستخدم EntityGraph
    @EntityGraph(attributePaths = { "contract", "contract.tenant" })
    List<RentRecord> findByPaidFalseAndDueDateLessThanEqualOrderByDueDateAsc(LocalDate currentDate);

    @EntityGraph(attributePaths = { "contract", "contract.tenant" })
    List<RentRecord> findByPaidFalseAndDueDateBetweenOrderByDueDateAsc(LocalDate startDate, LocalDate endDate);

    // الدالة الإضافية التي اقترحناها سابقاً (يمكنك حذفها إذا لم تستخدمها)
    @Query("SELECT r FROM RentRecord r " +
            "JOIN FETCH r.contract c " +
            "JOIN FETCH c.tenant t " +
            "WHERE r.paid = false AND r.dueDate <= :date")
    List<RentRecord> findOverduePayments(@Param("date") LocalDate date);

}