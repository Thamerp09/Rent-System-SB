package com.thamer.Rent_System.controller.rest; // <--- حزمة الـ Controller الصحيحة

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // استيراد مهم لـ Authentication
import org.springframework.web.bind.annotation.*;

// استيراد الخدمات. تأكد من أن هذه الخدمات موجودة في حزمة service.*
import com.thamer.Rent_System.service.RentalManagementService;
import com.thamer.Rent_System.service.RentalContractService;

// استيراد DTOs والـ Enums والـ Entities
// استيراد DTOs من حزمتها الجديدة (dto)
import com.thamer.Rent_System.model.*; // <--- استيراد من حزمة الـ DTOs

// استيراد باقي الـ DTOs والـ Entities والـ Enums من حزمة الـ model
import com.thamer.Rent_System.model.DashboardResponseDTO;
import com.thamer.Rent_System.model.PropertyLocation;
import com.thamer.Rent_System.model.TenantContractForm;
import com.thamer.Rent_System.model.Tenant;
import com.thamer.Rent_System.model.RentalContract;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * هذا الملف يجمع كل Rest Controllers في مكان واحد لتبسيط الاستخدام مع Flutter.
 * يحتوي على Endpoints لـ:
 * - لوحة التحكم (Dashboard)
 * - إدارة العقود والمستأجرين (Contracts)
 * - معلومات المستخدم الحالي (User)
 * - إدارة سجلات الدفع (Rent Records)
 *
 * تم تحديث هذا الـ Controller لاستخدام DTOs التي تم إنشاؤها الآن في حزمة 'dto'.
 */
@RestController
@RequestMapping("/api") // مسار أساسي موحد لكل الـ APIs
public class AllInOneRestController {

    @Autowired
    private RentalManagementService rentalManagementService;
    @Autowired
    private RentalContractService rentalContractService;

    // --- 1. Dashboard Endpoints ---
    @GetMapping("/dashboard")
    public DashboardResponseDTO getDashboardData() {
        DashboardResponseDTO dashboardData = new DashboardResponseDTO();

        dashboardData.setFinancialSummary(rentalManagementService.getOverallFinancialSummary());
        dashboardData.setDueNowPayments(rentalManagementService.getDueNowPayments());
        dashboardData.setUpcomingPayments(rentalManagementService.getUpcomingPayments());
        dashboardData.setMonthlyRevenueData(rentalManagementService.getMonthlyExpectedRevenueForCurrentYear());

        return dashboardData;
    }

    // --- 2. Contracts Endpoints ---
    @GetMapping("/contracts")
    public ContractsListResponseDTO getAllContractsData() {
        ContractsListResponseDTO data = new ContractsListResponseDTO();

        data.setTenantsContracts(rentalManagementService.getAllCombinedData());
        data.setMakkahTotalRent(rentalManagementService.calculateTotalRentByLocation(PropertyLocation.MAKKAH));
        data.setMakkahContractsCount(rentalManagementService.countContractsByLocation(PropertyLocation.MAKKAH));
        data.setRiyadhTotalRent(rentalManagementService.calculateTotalRentByLocation(PropertyLocation.RIYADH));
        data.setRiyadhContractsCount(rentalManagementService.countContractsByLocation(PropertyLocation.RIYADH));

        return data;
    }

    @PostMapping("/contracts")
    public ResponseEntity<TenantContractForm> addTenant(@RequestBody TenantContractForm formData) {
        Tenant savedTenant = rentalManagementService.saveTenantAndReturn(formData.getTenant());

        RentalContract contract = formData.getContract();
        contract.setTenant(savedTenant);
        RentalContract savedContract = rentalContractService.saveContractAndReturn(contract);

        rentalContractService.generatePaymentRecords(savedContract);

        formData.getTenant().setId(savedTenant.getId());
        formData.getContract().setId(savedContract.getId());

        return new ResponseEntity<>(formData, HttpStatus.CREATED);
    }

    @DeleteMapping("/contracts/{id}")
    public ResponseEntity<Void> deleteContract(@PathVariable("id") Long id) {
        rentalManagementService.deleteContractAndAssociatedTenant(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // --- 3. User Endpoints ---
    @GetMapping("/user/me")
    public ResponseEntity<UserInfoDTO> getLoggedInUser(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            String displayName = username;

            if ("thamer".equals(username)) {
                displayName = "ثامر";
            } else if ("nadia".equals(username)) {
                displayName = "نادية";
            }
            return new ResponseEntity<>(new UserInfoDTO(username, displayName), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    // --- 4. Rent Records Endpoints ---
    @PatchMapping("/rent-records/{recordId}/toggle-paid")
    public ResponseEntity<Void> togglePaymentStatus(@PathVariable("recordId") Long recordId) {
        rentalManagementService.togglePaymentStatus(recordId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}