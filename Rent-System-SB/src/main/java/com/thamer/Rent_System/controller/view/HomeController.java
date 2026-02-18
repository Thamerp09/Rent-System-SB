package com.thamer.Rent_System.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.thamer.Rent_System.service.*;
import com.thamer.Rent_System.model.*;
import org.springframework.security.core.Authentication; // <<-- استيراد مهم
import java.math.BigDecimal;
import java.util.*;

@Controller
public class HomeController {

	// @Autowired
	// private TenantService tenantService;

	@Autowired
	private RentalManagementService rentalManagementService;
	@Autowired
	private RentalContractService rentaclContractService;

	@Autowired
	private RentRecordService rentRecordService;
	@Autowired
	private UserService userService;

	@GetMapping("/")
	public String root() {
		// هذا الأمر سيجبر النظام يروح لصفحة الدخول
		return "redirect:/dashboard";
	}

	@GetMapping("/login")
	public String loginPage() {
		return "login";
	}

	@GetMapping("/home")
	public String homePage(Model model, Authentication authentication) {

		if (authentication != null && authentication.isAuthenticated()) {
			String username = authentication.getName();
			String displayName = username;

			if ("thamer".equals(username)) {
				displayName = "ثامر";
			} else if ("nadia".equals(username)) {
				displayName = "نادية";
			}
			model.addAttribute("loggedInUsername", displayName);
		}
		model.addAttribute("pendingCount", userService.countPendingUsers());
		model.addAttribute("pendingUsers", userService.getPendingUsers());

		return "home";
	}

	@GetMapping("/register")
	public String showForm(@RequestParam(value = "id", required = false) Long id, Model model) {
		if (id != null) {
			// إذا وجد ID، نبحث عن البيانات المخزنة لملء النموذج
			TenantContractForm existingData = rentalManagementService.getCombinedDataByContractId(id);
			model.addAttribute("tenantForm", existingData);
			model.addAttribute("isEdit", true);
		} else {
			// إذا لم يوجد ID، نفتح نموذجاً فارغاً لإضافة عقد جديد
			model.addAttribute("tenantForm", new TenantContractForm());
			model.addAttribute("isEdit", false);
		}
		return "register";
	}

	@GetMapping("/list")
	public String listAllData(Model model) {
		// استدع الدالة التي تقوم بكل العمل
		List<TenantContractForm> allData = rentalManagementService.getAllCombinedData();
		// أرسل الzقائمة المجمعة إلى HTML تحت اسم "tenants"
		model.addAttribute("tenants", allData);

		// 1. حساب إيرادات مكة
		BigDecimal makkahRent = rentalManagementService.calculateTotalRentByLocation(PropertyLocation.MAKKAH);
		model.addAttribute("makkahTotalRent", makkahRent);

		// 2. حساب عدد عقود مكة
		long makkahCount = rentalManagementService.countContractsByLocation(PropertyLocation.MAKKAH);
		model.addAttribute("makkahContractsCount", makkahCount);

		// 3. حساب إيرادات الرياض
		BigDecimal riyadhRent = rentalManagementService.calculateTotalRentByLocation(PropertyLocation.RIYADH);
		model.addAttribute("riyadhTotalRent", riyadhRent);

		// 4. حساب عدد عقود الرياض
		long riyadhCount = rentalManagementService.countContractsByLocation(PropertyLocation.RIYADH);
		model.addAttribute("riyadhContractsCount", riyadhCount);

		model.addAttribute("currentPage", "list");

		return "list";
	}

	@PostMapping("/addTenant")
	public String addTenant(@ModelAttribute("tenantForm") TenantContractForm formData) {

		// --- 1. احفظ المستأجر ---
		Tenant savedTenant = rentalManagementService.saveTenantAndReturn(formData.getTenant());

		// --- 2. احفظ العقد بعد ربطه بالمستأجر ---
		RentalContract contract = formData.getContract();
		contract.setTenant(savedTenant);
		RentalContract savedContract = rentaclContractService.saveContractAndReturn(contract);

		// // --- 3. إنشاء وحفظ أول سجل دفع تلقائيًا ---
		// RentRecord firstRecord = new RentRecord();
		// firstRecord.setContract(savedContract);
		// firstRecord.setDueDate(savedContract.getContractStart());
		// firstRecord.setAmount(savedContract.getRentAmount());
		// firstRecord.setPaid(false);
		//
		// rentRecordService.saveRecord(firstRecord);

		rentaclContractService.generatePaymentRecords(savedContract);
		// --- 4. أعد التوجيه إلى صفحة القائمة ---
		return "redirect:/list";
	}

	// @PostMapping("/updateContract")
	// public String updateContract(TenantContractForm updatedFormData) { // تم حذف
	// @ModelAttribute لتبسيط الربط
	//
	// // 1. تحديث بيانات المستأجر (مهم لتحديث الاسم)
	// Tenant tenantData = updatedFormData.getTenant();
	// if (tenantData != null && tenantData.getId() != null) {
	// rentalManagementService.updateTenantName(tenantData.getId(),
	// tenantData.getName());
	// }
	//
	// // 2. تحديث بيانات العقد
	// RentalContract contractData = updatedFormData.getContract();
	// if (contractData != null && contractData.getId() != null) {
	// rentaclContractService.updateContract(contractData.getId(), contractData);
	// }
	//
	// return "redirect:/list";
	// }

	// الدالة التي تستقبل طلب الحذف
	@PostMapping("/contracts/delete/{id}")
	public String deleteContract(@PathVariable("id") Long id) {
		rentalManagementService.deleteContractAndAssociatedTenant(id);
		// إعادة توجيه إلى نفس الصفحة الرئيسية لتحديث القائمة
		return "redirect:/list";
	}

	@PostMapping("/records/toggle-status/{recordId}")
	public String togglePaymentStatus(@PathVariable("recordId") Long recordId) {
		rentalManagementService.togglePaymentStatus(recordId); // استدعاء الدالة الجديدة

		// أعد التوجيه إلى الصفحة الرئيسية مع تحديث
		return "redirect:/list";
	}

	@GetMapping("/api/pending-users")
	@ResponseBody // هذه تعني: أرسل بيانات فقط ولا ترسل صفحة HTML
	public Map<String, Object> getPendingUsersApi() {
		Map<String, Object> response = new HashMap<>();

		// جلب القائمة والعدد
		List<UserEntity> pendingUsers = userService.getPendingUsers();

		// نرسل العدد والقائمة
		response.put("count", pendingUsers.size());
		response.put("users", pendingUsers);

		return response;
	}

	// 1. عرض صفحة المستخدمين
	@GetMapping("/users")
	public String listUsers(Model model) {
		model.addAttribute("users", userService.getAllUsers());
		return "users-list"; // اسم ملف الـ HTML
	}

	// 2. حذف مستخدم بناءً على الـ ID
	@PostMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable("id") Long id) {
		userService.deleteUserById(id);
		return "redirect:/users?deleted"; // إعادة توجيه للصفحة بعد الحذف
	}

}
