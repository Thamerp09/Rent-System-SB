//package com.thamer.Rent_System.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import com.thamer.Rent_System.repository.*;
//
//import java.math.BigDecimal;
//import java.util.*;
//import com.thamer.Rent_System.model.*;
//@Service
//public class TenantService {
//
//	
//	@Autowired
//	private TenantRepository tenantRepository;
//	   
//    @Autowired
//    private RentalContractRepository rentalContractRepository;
//    @Autowired
//    private RentRecordRepository rentRecordRepository;
//
//    /**
//     * هذه هي الدالة التي تجمع البيانات من كل الجداول.
//     */
//    public List<TenantContractForm> getAllCombinedData() {
//        
//        // 1. احصل على قائمة بكل المستأجرين من قاعدة البيانات
//        List<Tenant> allTenants = tenantRepository.findAll();
//        
//        // 2. أنشئ قائمة فارغة جديدة لتخزين البيانات المجمعة
//        List<TenantContractForm> combinedDataList = new ArrayList<>();
//        
//        // 3. لكل مستأجر في القائمة...
//        for (Tenant tenant : allTenants) {
//            
//            // 4. أنشئ "صندوق" جديد (DTO) لهذا المستأجر
//            TenantContractForm form = new TenantContractForm();
//            
//            // 5. ضع معلومات المستأجر الأساسية في الصندوق
//            form.setTenant(tenant);
//            
//            // 6. ابحث عن العقد المرتبط بهذا المستأجر
//            RentalContract contract = rentalContractRepository.findByTenant(tenant);
//            if (contract != null) {
//                // إذا وجدنا العقد، ضعه في الصندوق
//                form.setContract(contract);
//                
//                // 7. الآن ابحث عن آخر سجل دفع لهذا العقد
//                RentRecord record = rentRecordRepository.findTopByContractOrderByDueDateDesc(contract);
//                if (record != null) {
//                    // إذا وجدنا سجل دفع، ضعه في الصندوق
//                    form.setRecord(record);
//                }
//            }
//            
//            // 8. أضف الصندوق المكتمل إلى قائمتنا النهائية
//            combinedDataList.add(form);
//        }
//        
//        // 9. أعد القائمة المجمعة التي تحتوي على كل شيء
//        return combinedDataList;
//    }
//
//    // ... باقي دوالك
//
//	
//	public List<Tenant> getAllTenants(){
//		return tenantRepository.findAll();
//	}
//	
//	public Optional<Tenant> getTenantById(Long id){
//		return tenantRepository.findById(id);
//	}
//	
//	public void saveTenant(Tenant tenant) {
//		tenantRepository.save(tenant);
//	}
//	
//	public void deleteTenantById(Long id) {
//		tenantRepository.deleteById(id);
//	}
//	  public long countContractsByLocation(PropertyLocation location) {
//	        
//	        // استدعاء الدالة مباشرة من الـ repository
//	        return rentalContractRepository.countByLocation(location);
//	    }
//
//	
//	  public BigDecimal calculateTotalRentByLocation(PropertyLocation location) {
//	        
//	        // 1. استدعاء دالة من الـ repository لجلب كل العقود في هذا الموقع.
//	        List<RentalContract> contractsInLocation = rentalContractRepository.findByLocation(location);
//	        
//	        // 2. تهيئة متغير لتخزين المجموع، نبدأ من صفر.
//	        BigDecimal totalRent = BigDecimal.ZERO;
//	        
//	        // 3. المرور على كل عقد في القائمة التي حصلنا عليها.
//	        for (RentalContract contract : contractsInLocation) {
//	            
//	            // 4. نتأكد من أن مبلغ الإيجار ليس null لتجنب الأخطاء.
//	            if (contract.getRentAmount() != null) {
//	                
//	                // 5. نضيف مبلغ إيجار العقد الحالي إلى المجموع الكلي.
//	                totalRent = totalRent.add(contract.getRentAmount());
//	            }
//	        }
//	        
//	        // 6. نعيد المجموع النهائي.
//	        return totalRent;
//	    }
//	  public Tenant saveTenantAndReturn(Tenant tenant) {
//		    return tenantRepository.save(tenant);
//		}
//	  
//	  
//	  
//	  public Tenant updateTenantName(Long tenantId, String newName) {
//	        
//	        // 1. ابحث عن المستأجر في قاعدة البيانات
//	        // orElseThrow يطلق خطأ إذا لم يتم العثور على المستأجر
//	        Tenant existingTenant = tenantRepository.findById(tenantId)
//	                .orElseThrow(() -> new RuntimeException("لم يتم العثور على المستأجر بالهوية: " + tenantId));
//	        
//	        // 2. قم بتحديث الاسم
//	        existingTenant.setName(newName);
//	        
//	        // 3. احفظ الكائن المحدث في قاعدة البيانات
//	        return tenantRepository.save(existingTenant);
//	    }
//}
