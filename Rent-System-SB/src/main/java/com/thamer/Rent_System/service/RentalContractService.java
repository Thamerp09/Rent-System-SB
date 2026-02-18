package com.thamer.Rent_System.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.thamer.Rent_System.repository.*;
import com.thamer.Rent_System.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;
import java.time.temporal.*;
import com.thamer.Rent_System.model.ContractStatus;
@Service
public class RentalContractService {

	
	@Autowired
	private RentalContractRepository repository;
	
	@Autowired 
	private RentRecordRepository rentRecordRepository;
	
	public List<RentalContract> getContractByTenantId(Long TenantID){
		return repository.findByTenant_Id(TenantID);
	}
	
	public void saveContract(RentalContract contract) {
		repository.save(contract);
	}
	public RentalContract saveContractAndReturn(RentalContract contract) {
	    return repository.save(contract);
	}
	
public RentalContract updateContract(Long contractId, RentalContract updatedContractData) {
        
        // 1. ابحث عن العقد الأصلي في قاعدة البيانات باستخدام الـ ID.
        // orElseThrow سيطلق خطأ إذا لم يتم العثور على العقد.
        RentalContract existingContract = repository.findById(contractId)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على العقد بالهوية: " + contractId));

        // 2. الآن، قم بتحديث حقول العقد الأصلي بالبيانات الجديدة.
        existingContract.setContractEnd(updatedContractData.getContractEnd());
        existingContract.setRentAmount(updatedContractData.getRentAmount());
        // يمكنك إضافة أي حقول أخرى تريد السماح بتعديلها هنا
        // مثلاً: existingContract.setPaymentType(updatedContractData.getPaymentType());
        
        // 3. احفظ التغييرات في قاعدة البيانات.
        return repository.save(existingContract);
    }
//==== أضف هذه الدالة الجديدة والمهمة هنا ====
/**
 * تقوم بإنشاء جدول دفعات تلقائي بناءً على نوع العقد.
 * @param contract العقد الذي تم حفظه للتو.
 */
public void generatePaymentRecords(RentalContract contract) {
    
    PaymentType paymentType = contract.getPaymentType();
    LocalDate startDate = contract.getContractStart();
    BigDecimal totalAmount = contract.getRentAmount();

    // قائمة لتخزين كل الدفعات التي سننشئها
    List<RentRecord> recordsToSave = new ArrayList<>();

    // تأكد من أن البيانات الأساسية موجودة
    if (paymentType == null || startDate == null || totalAmount == null) {
        // لا تفعل شيئًا إذا كانت المعلومات ناقصة
        return; 
    }

    if (paymentType == PaymentType.ANNUALLY) {
        // دفعة واحدة فقط في بداية العقد
        recordsToSave.add(new RentRecord(contract, startDate, totalAmount));

    } else if (paymentType == PaymentType.SEMI_ANNUALLY) {
        // دفعتان كل 6 أشهر
        BigDecimal amountPerPayment = totalAmount.divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP);
        recordsToSave.add(new RentRecord(contract, startDate, amountPerPayment));
        recordsToSave.add(new RentRecord(contract, startDate.plusMonths(6), amountPerPayment));

    } else if (paymentType == PaymentType.QUARTERLY) {
        // 4 دفعات كل 3 أشهر
        BigDecimal amountPerPayment = totalAmount.divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);
        for (int i = 0; i < 4; i++) {
            recordsToSave.add(new RentRecord(contract, startDate.plusMonths(i * 3L), amountPerPayment));
        }

    } else if (paymentType == PaymentType.MONTHLY) {
        // 12 دفعة كل شهر
        BigDecimal amountPerPayment = totalAmount.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP);
        for (int i = 0; i < 12; i++) {
            recordsToSave.add(new RentRecord(contract, startDate.plusMonths(i), amountPerPayment));
        }
    }
    
    // إذا تم إنشاء أي سجلات، قم بحفظها كلها مرة واحدة في قاعدة البيانات
    if (!recordsToSave.isEmpty()) {
        rentRecordRepository.saveAll(recordsToSave);
    }
}

}