package com.thamer.Rent_System.service;

import com.thamer.Rent_System.model.*;
import com.thamer.Rent_System.repository.RentRecordRepository;
import com.thamer.Rent_System.repository.RentalContractRepository;
import com.thamer.Rent_System.repository.TenantRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

//-----
import java.time.Month;
import java.time.Year;
import java.util.LinkedHashMap; // مهم للحفاظ على ترتيب الشهور
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RentalManagementService {

    // 1. حقن (Inject) كل الـ Repositories التي نحتاجها في مكان واحد
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private RentalContractRepository rentalContractRepository;

    @Autowired
    private RentRecordRepository rentRecordRepository;

    // =================================================================
    // == قسم الميثودز الخاصة بالمستأجرين (Tenants) ==
    // =================================================================

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Optional<Tenant> getTenantById(Long id) {
        return tenantRepository.findById(id);
    }

    public void saveTenant(Tenant tenant) {
        tenantRepository.save(tenant);
    }

    public Tenant saveTenantAndReturn(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public void deleteTenantById(Long id) {
        tenantRepository.deleteById(id);
    }

    // i don't want this method
    // public Tenant updateTenantName(Long tenantId, String newName) {
    // Tenant existingTenant = tenantRepository.findById(tenantId)
    // .orElseThrow(() -> new RuntimeException("لم يتم العثور على المستأجر بالهوية:
    // " + tenantId));
    //
    // existingTenant.setName(newName);
    //
    // return tenantRepository.save(existingTenant);
    // }

    // =================================================================
    // == قسم الميثودز الخاصة بالعقود (Contracts) ==
    // =================================================================

    public List<RentalContract> getContractsByTenantId(Long tenantId) {
        return rentalContractRepository.findByTenant_Id(tenantId);
    }

    public void saveContract(RentalContract contract) {
        rentalContractRepository.save(contract);
    }

    public RentalContract saveContractAndReturn(RentalContract contract) {
        return rentalContractRepository.save(contract);
    }

    public RentalContract updateContract(Long contractId, RentalContract updatedContractData) {
        RentalContract existingContract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على العقد بالهوية: " + contractId));

        existingContract.setContractStart(updatedContractData.getContractStart());
        existingContract.setContractEnd(updatedContractData.getContractEnd());
        existingContract.setRentAmount(updatedContractData.getRentAmount());

        return rentalContractRepository.save(existingContract);
    }

    // =================================================================
    // == قسم الميثودز الخاصة بسجلات الدفع (Rent Records) ==
    // =================================================================

    public void saveRecord(RentRecord record) {
        rentRecordRepository.save(record);
    }

    public List<RentRecord> getAllRecords() {
        return rentRecordRepository.findAll();
    }

    public Optional<RentRecord> getRecordById(Long id) {
        return rentRecordRepository.findById(id);
    }

    public void deleteRecord(Long id) {
        rentRecordRepository.deleteById(id);
    }

    public RentRecord updateRecord(Long id, RentRecord updatedRecord) {
        return rentRecordRepository.findById(id)
                .map(record -> {
                    record.setAmount(updatedRecord.getAmount());
                    record.setDueDate(updatedRecord.getDueDate());
                    record.setPaid(updatedRecord.isPaid());
                    record.setContract(updatedRecord.getContract());
                    return rentRecordRepository.save(record);
                }).orElseThrow(() -> new RuntimeException("Record not found with ID: " + id));
    }

    // =================================================================
    // == قسم الميثودز المجمعة والعمليات المعقدة (Business Logic) ==
    // =================================================================

    /**
     * هذه الدالة تجمع البيانات من كل الجداول (Tenant, Contract, Record).
     */
    public List<TenantContractForm> getAllCombinedData() {
        List<Tenant> allTenants = tenantRepository.findAll();
        List<TenantContractForm> combinedDataList = new ArrayList<>();

        for (Tenant tenant : allTenants) {
            TenantContractForm form = new TenantContractForm();
            form.setTenant(tenant);

            RentalContract contract = rentalContractRepository.findByTenant(tenant);
            if (contract != null) {
                form.setContract(contract);

                form.setContractStatus(determineContractStatus(contract.getContractEnd()));

                RentRecord record = rentRecordRepository.findTopByContractOrderByDueDateDesc(contract);
                if (record != null) {
                    form.setRecord(record);
                }
            }
            combinedDataList.add(form);
        }
        return combinedDataList;
    }

    /**
     * تقوم بإنشاء جدول دفعات تلقائي بناءً على نوع العقد.
     */
    public void generatePaymentRecords(RentalContract contract) {
        PaymentType paymentType = contract.getPaymentType();
        LocalDate startDate = contract.getContractStart();
        BigDecimal totalAmount = contract.getRentAmount();

        List<RentRecord> recordsToSave = new ArrayList<>();

        if (paymentType == null || startDate == null || totalAmount == null) {
            return;
        }

        if (paymentType == PaymentType.ANNUALLY) {
            recordsToSave.add(new RentRecord(contract, startDate, totalAmount));
        } else if (paymentType == PaymentType.SEMI_ANNUALLY) {
            BigDecimal amountPerPayment = totalAmount.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
            recordsToSave.add(new RentRecord(contract, startDate, amountPerPayment));
            recordsToSave.add(new RentRecord(contract, startDate.plusMonths(6), amountPerPayment));
        } else if (paymentType == PaymentType.QUARTERLY) {
            BigDecimal amountPerPayment = totalAmount.divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);
            for (int i = 0; i < 4; i++) {
                recordsToSave.add(new RentRecord(contract, startDate.plusMonths(i * 3L), amountPerPayment));
            }
        } else if (paymentType == PaymentType.MONTHLY) {
            BigDecimal amountPerPayment = totalAmount.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
            for (int i = 0; i < 12; i++) {
                recordsToSave.add(new RentRecord(contract, startDate.plusMonths(i), amountPerPayment));
            }
        }

        if (!recordsToSave.isEmpty()) {
            rentRecordRepository.saveAll(recordsToSave);
        }
    }

    /**
     * يحسب عدد العقود في موقع معين.
     */
    public long countContractsByLocation(PropertyLocation location) {
        return rentalContractRepository.countByLocation(location);
    }

    /**
     * يحسب إجمالي قيمة الإيجارات في موقع معين.
     */
    public BigDecimal calculateTotalRentByLocation(PropertyLocation location) {
        List<RentalContract> contractsInLocation = rentalContractRepository.findByLocation(location);
        BigDecimal totalRent = BigDecimal.ZERO;

        for (RentalContract contract : contractsInLocation) {
            if (contract.getRentAmount() != null) {
                totalRent = totalRent.add(contract.getRentAmount());
            }
        }
        return totalRent;
    }

    // في RentalManagementService.java

    // في RentalManagementService.java

    // في RentalManagementService.java

    @Transactional
    public void deleteContractAndAssociatedTenant(Long contractId) {
        // 1. ابحث عن العقد الذي نريد حذفه
        RentalContract contractToDelete = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على عقد بالرقم: " + contractId));

        // 2. احصل على المستأجر المرتبط به
        Tenant tenant = contractToDelete.getTenant();

        // 3. الخطوة السحرية: قم بإزالة العقد من قائمة عقود المستأجر
        if (tenant != null && tenant.getRentalContract() != null) {
            tenant.getRentalContract().remove(contractToDelete);
        }

        // 4. الآن، احذف العقد. JPA الآن يراه ككائن يتيم وسيحذفه بسهولة.
        rentalContractRepository.delete(contractToDelete);

        // 5. تحقق مما إذا كان المستأجر أصبح بلا عقود
        if (tenant != null) {
            // نعيد تحميل المستأجر للتأكد من حالته بعد الحذف
            tenantRepository.findById(tenant.getId()).ifPresent(freshTenant -> {
                if (freshTenant.getRentalContract() == null || freshTenant.getRentalContract().isEmpty()) {
                    tenantRepository.deleteById(freshTenant.getId());
                }
            });
        }
    }

    @Transactional
    public RentRecord togglePaymentStatus(Long recordId) {
        // 1. ابحث عن السجل.
        RentRecord recordToUpdate = rentRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على سجل الدفع بالرقم: " + recordId));

        // 2. اعكس القيمة المنطقية (boolean).
        // إذا كانت true، ستصبح false. وإذا كانت false، ستصبح true.
        recordToUpdate.setPaid(!recordToUpdate.isPaid());

        // 3. احفظ التغييرات وأرجع الكائن المحدث.
        return rentRecordRepository.save(recordToUpdate);
    }

    // // يحسب الملخص المالي للسنة الحالية
    // public FinancialSummaryDTO getFinancialSummaryForCurrentYear() {
    // FinancialSummaryDTO summary = new FinancialSummaryDTO();
    // int currentYear = Year.now().getValue();
    //
    // // احسب المبالغ باستخدام دوال الريبوزيتوري
    //
    // BigDecimal totalCollected =
    // rentRecordRepository.calculateTotalByYearAndPaidStatus(currentYear, true);
    // BigDecimal totalExpected =
    // rentRecordRepository.calculateTotalByYear(currentYear);
    //
    // summary.setTotalCollected(totalCollected);
    // summary.setTotalExpected(totalExpected);
    // summary.setTotalRemaining(totalExpected.subtract(totalCollected));
    //
    // return summary;
    // }

    public FinancialSummaryDTO getOverallFinancialSummary() {
        FinancialSummaryDTO summary = new FinancialSummaryDTO();

        // إجمالي المبالغ المحصلة لكل السنوات
        BigDecimal totalCollected = rentRecordRepository.calculateTotalCollectedOverall();
        summary.setTotalCollected(totalCollected);

        // إجمالي المبالغ المتوقعة لكل السنوات
        BigDecimal totalExpected = rentRecordRepository.calculateTotalExpectedOverall();
        summary.setTotalExpected(totalExpected);

        // المبلغ المتبقي = إجمالي المتوقع الكلي - إجمالي المحصل الكلي
        summary.setTotalRemaining(totalExpected.subtract(totalCollected));

        // *********** حساب الإجماليات المسحقة والقادمة *****//

        // جلب الدفعات المستحقة الآن
        List<RentRecord> dueNowPayments = getDueNowPayments();
        BigDecimal totalDueNow = dueNowPayments.stream().map(RentRecord::getAmount).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        summary.setTotalDueNowPayments(totalDueNow);

        // جلب الدفعات القادمة
        List<RentRecord> upcomingPayments = getUpcomingPayments();
        BigDecimal totalUpcoming = upcomingPayments.stream().map(RentRecord::getAmount).reduce(BigDecimal.ZERO,
                BigDecimal::add);
        summary.setTotalUpcomingPayments(totalUpcoming);

        return summary;
    }

    // يجلب قائمة بالدفعات المتحقة الآن(والتي فات موعدها)
    public List<RentRecord> getDueNowPayments() {

        LocalDate today = LocalDate.now();
        return rentRecordRepository.findByPaidIsFalseAndDueDateLessThanEqual(today);

    }

    // يجلب قائمة بالدفعات القادمة خلال الشهرين القادمين
    public List<RentRecord> getUpcomingPayments() {
        // تاريخ اليوم
        LocalDate today = LocalDate.now();
        // تاريخ البداية هو غدا ً
        LocalDate startDate = today.plusDays(1);
        // تاريخ النهاية هو بعد شهرين من الآن
        LocalDate endDate = today.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());

        // 1. جلب الدفعات الحقيقية الموجودة في النظام
        List<RentRecord> actualPayments = rentRecordRepository.findByPaidIsFalseAndDueDateBetween(startDate, endDate);
        
        // ننشئ قائمة جديدة قابلة للتعديل لنضع فيها الكل
        List<RentRecord> combinedPayments = new ArrayList<>(actualPayments);

        // 2. الذكاء البرمجي: حساب التوقعات للعقود المنتهية
        List<RentalContract> allContracts = rentalContractRepository.findAll(); // يمكن تحسينها لجلب المنتهية فقط لاحقاً

        for (RentalContract contract : allContracts) {
            // نتخطى العقود السارية التي لها سجلات بالفعل، نركز على المنتهي أو الذي سينتهي قريباً
            if (contract.getContractEnd().isBefore(endDate)) {
                
                // تحديد دورة الدفع (كم شهر نزيد؟)
                int monthsToAdd = 0;
                if (contract.getPaymentType() == PaymentType.MONTHLY) monthsToAdd = 1;
                else if (contract.getPaymentType() == PaymentType.QUARTERLY) monthsToAdd = 3;
                else if (contract.getPaymentType() == PaymentType.SEMI_ANNUALLY) monthsToAdd = 6;
                else if (contract.getPaymentType() == PaymentType.ANNUALLY) monthsToAdd = 12;

                if (monthsToAdd == 0) continue; // تخطي إذا لم يوجد نوع دفع

                // نبدأ الحساب من تاريخ نهاية العقد
                LocalDate nextPotentialDate = contract.getContractEnd().plusDays(1); // يبدأ العقد الجديد في اليوم التالي

                // نقوم بزيادة التواريخ حتى نصل إلى فترة المستقبل (الشهرين القادمين)
                // هذا اللوب يعالج العقود المنتهية منذ زمن طويل أيضاً ويحضر أقرب دفعة مستقبلية
                while (nextPotentialDate.isBefore(startDate)) {
                    nextPotentialDate = nextPotentialDate.plusMonths(monthsToAdd);
                }

                // الآن: هل التاريخ المحسوب يقع ضمن الشهرين القادمين؟
                if (!nextPotentialDate.isAfter(endDate)) {
                    // نعم! أنشئ سجلاً وهمياً (توقع)
                    RentRecord projectedRecord = new RentRecord();
                    projectedRecord.setContract(contract);
                    projectedRecord.setDueDate(nextPotentialDate);
                    
                    // حساب المبلغ المتوقع (للدفعة الواحدة)
                    BigDecimal totalAmount = contract.getRentAmount();
                    BigDecimal paymentAmount = totalAmount; // الافتراضي
                    
                    // تقسيم المبلغ حسب نوع الدفع
                    if (contract.getPaymentType() == PaymentType.MONTHLY) 
                        paymentAmount = totalAmount.divide(new BigDecimal(12), 2, RoundingMode.HALF_UP);
                    else if (contract.getPaymentType() == PaymentType.QUARTERLY)
                        paymentAmount = totalAmount.divide(new BigDecimal(4), 2, RoundingMode.HALF_UP);
                    else if (contract.getPaymentType() == PaymentType.SEMI_ANNUALLY)
                        paymentAmount = totalAmount.divide(new BigDecimal(2), 2, RoundingMode.HALF_UP);
                    
                    projectedRecord.setAmount(paymentAmount);
                    projectedRecord.setPaid(false);
                    projectedRecord.setProjected(true); // <--- هام جداً: وضع علامة أنه متوقع

                    combinedPayments.add(projectedRecord);
                }
            }
        }

        // ---------------------------------------------------------
        // 3. التعديل هنا: الترتيب (الحقيقي فوق والمتوقع تحت)
        // ---------------------------------------------------------
        combinedPayments.sort(Comparator
                .comparing(RentRecord::isProjected) // الحقيقي (false) أولاً، المتوقع (true) آخراً
                .thenComparing(RentRecord::getDueDate)); // ترتيب داخلي حسب التاريخ

        return combinedPayments;
    
    }

    public List<ContractDisplayDTO> getAllContractsWithStatus() {
        // جلب جميع عفود الايجار
        List<RentalContract> contracts = rentalContractRepository.findAll();

        return contracts.stream()
                .map(this::mapToContractDisplayDTO)
                .collect(Collectors.toList());
    }

    private ContractDisplayDTO mapToContractDisplayDTO(RentalContract contract) {
        ContractDisplayDTO dto = new ContractDisplayDTO();
        dto.setContractId(contract.getId());

        // التأكد من وجود المستأجر قبل محاولة الوصول الى اسمه
        dto.setTenantName(contract.getTenant() != null ? contract.getTenant().getName() : "مستأجر غير معروف");

        dto.setContractStart(contract.getContractStart());
        dto.setContractEnd(contract.getContractEnd());
        dto.setRentAmount(contract.getRentAmount());
        dto.setPaymentType(contract.getPaymentType());
        dto.setLocation(contract.getLocation());

        // حساب وتعيين حالة العقد
        dto.setStatus(determineContractStatus(contract.getContractEnd()));

        return dto;
    }

    private ContractStatus determineContractStatus(LocalDate contractEnd) {

        LocalDate today = LocalDate.now();

        if (contractEnd.isBefore(today)) {
            return ContractStatus.EXPIRED;
        }

        // if (contractEnd == null) {
        // return ContractStatus.ACTIVE;
        // }

        // حساب عدد الايام المتبقية حتى تاريخ الانتهاء
        long daysUntilEnd = ChronoUnit.DAYS.between(today, contractEnd);

        if (daysUntilEnd <= 60) {
            return ContractStatus.EXPIRING_SOON;
        }

        return ContractStatus.ACTIVE;

    }

    // داخل كلاس الـ Service
    public Map<String, BigDecimal> getMonthlyExpectedRevenueForCurrentYear() {
        List<RentRecord> allRecords = rentRecordRepository.findAll(); // أو الطريقة التي تجلب بها كل سجلات الدفع

        // استخدام LinkedHashMap للحفاظ على ترتيب الشهور
        Map<Month, BigDecimal> monthlyTotals = new LinkedHashMap<>();

        // تهيئة الخريطة بجميع الشهور وقيمة صفر
        for (Month month : Month.values()) {
            monthlyTotals.put(month, BigDecimal.ZERO);
        }

        int currentYear = Year.now().getValue();

        allRecords.stream()
                // فلترة السجلات للسنة الحالية فقط
                .filter(record -> record.getDueDate().getYear() == currentYear)
                .forEach(record -> {
                    Month month = record.getDueDate().getMonth();
                    BigDecimal currentTotal = monthlyTotals.get(month);
                    // إضافة مبلغ السجل إلى المجموع الشهري
                    monthlyTotals.put(month, currentTotal.add(record.getAmount()));
                });

        // تحويل الخريطة إلى <String, BigDecimal> لتسهيل استخدامها في JavaScript
        return monthlyTotals.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(), // اسم الشهر (e.g., "JANUARY")
                        Map.Entry::getValue,
                        (v1, v2) -> v1, // في حال وجود مفاتيح مكررة (لا يفترض أن يحدث)
                        LinkedHashMap::new));
    }

    public TenantContractForm getCombinedDataByContractId(Long contractId) {
        // 1. جلب العقد من قاعدة البيانات
        RentalContract contract = rentalContractRepository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("Contract not found"));

        // 2. إنشاء كائن النموذج وتعبئته
        TenantContractForm form = new TenantContractForm();
        form.setContract(contract);
        form.setTenant(contract.getTenant()); // ربط المستأجر الموجود داخل العقد

        return form;
    }

    @Transactional
    public void updatePaymentAmount(Long recordId, BigDecimal newPaidAmount) {
        RentRecord record = rentRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("الدفعة غير موجودة"));

        // تحديث المبلغ المدفوع
        record.setPaidAmount(newPaidAmount);

        // منطق ذكي: هل المبلغ المدفوع يساوي أو أكبر من المستحق؟
        // إذا نعم -> نعتبرها مدفوعة بالكامل (paid = true)
        // إذا لا -> نعتبرها غير مدفوعة بالكامل (paid = false)
        if (newPaidAmount != null && newPaidAmount.compareTo(record.getAmount()) >= 0) {
            record.setPaid(true);
        } else {
            record.setPaid(false);
        }

        rentRecordRepository.save(record);
    }
}
