package com.thamer.Rent_System.model; // <--- حزمة الـ DTOs

import com.thamer.Rent_System.model.FinancialSummaryDTO; // تحتاج إلى استيراد هذا من حزمة الـ model الخاصة بك
import com.thamer.Rent_System.model.RentRecord;         // تحتاج إلى استيراد هذا من حزمة الـ model الخاصة بك
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

// DTO for Dashboard
public class DashboardResponseDTO { // <--- يجب أن تكون public
    private FinancialSummaryDTO financialSummary;
    private List<RentRecord> dueNowPayments;
    private List<RentRecord> upcomingPayments;
    private Map<String, BigDecimal> monthlyRevenueData;

    // Getters and Setters
    public FinancialSummaryDTO getFinancialSummary() {
        return financialSummary;
    }

    public void setFinancialSummary(FinancialSummaryDTO financialSummary) {
        this.financialSummary = financialSummary;
    }

    public List<RentRecord> getDueNowPayments() {
        return dueNowPayments;
    }

    public void setDueNowPayments(List<RentRecord> dueNowPayments) {
        this.dueNowPayments = dueNowPayments;
    }

    public List<RentRecord> getUpcomingPayments() {
        return upcomingPayments;
    }

    public void setUpcomingPayments(List<RentRecord> upcomingPayments) {
        this.upcomingPayments = upcomingPayments;
    }

    public Map<String, BigDecimal> getMonthlyRevenueData() {
        return monthlyRevenueData;
    }

    public void setMonthlyRevenueData(Map<String, BigDecimal> monthlyRevenueData) {
        this.monthlyRevenueData = monthlyRevenueData;
    }
}