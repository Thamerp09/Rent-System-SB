package com.thamer.Rent_System.model; // <--- حزمة جديدة لـ DTOs

import com.thamer.Rent_System.model.TenantContractForm; // تحتاج إلى استيراد هذا من حزمة الـ model الخاصة بك
import com.thamer.Rent_System.model.PropertyLocation; // تحتاج إلى استيراد هذا من حزمة الـ model الخاصة بك
import java.math.BigDecimal;
import java.util.List;

// DTO for Contracts List Page
public class ContractsListResponseDTO { // <--- يجب أن تكون public
    private List<TenantContractForm> tenantsContracts;
    private BigDecimal makkahTotalRent;
    private long makkahContractsCount;
    private BigDecimal riyadhTotalRent;
    private long riyadhContractsCount;

    // Getters and Setters
    public List<TenantContractForm> getTenantsContracts() {
        return tenantsContracts;
    }

    public void setTenantsContracts(List<TenantContractForm> tenantsContracts) {
        this.tenantsContracts = tenantsContracts;
    }

    public BigDecimal getMakkahTotalRent() {
        return makkahTotalRent;
    }

    public void setMakkahTotalRent(BigDecimal makkahTotalRent) {
        this.makkahTotalRent = makkahTotalRent;
    }

    public long getMakkahContractsCount() {
        return makkahContractsCount;
    }

    public void setMakkahContractsCount(long makkahContractsCount) {
        this.makkahContractsCount = makkahContractsCount;
    }

    public BigDecimal getRiyadhTotalRent() {
        return riyadhTotalRent;
    }

    public void setRiyadhTotalRent(BigDecimal riyadhTotalRent) {
        this.riyadhTotalRent = riyadhTotalRent;
    }

    public long getRiyadhContractsCount() {
        return riyadhContractsCount;
    }

    public void setRiyadhContractsCount(long riyadhContractsCount) {
        this.riyadhContractsCount = riyadhContractsCount;
    }
}