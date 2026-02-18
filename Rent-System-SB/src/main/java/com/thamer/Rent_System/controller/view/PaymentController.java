package com.thamer.Rent_System.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.thamer.Rent_System.service.*;
import com.thamer.Rent_System.model.*;
import com.thamer.Rent_System.repository.RentRecordRepository;

import org.springframework.security.core.Authentication; // <<-- استيراد مهم
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private RentalManagementService rentalService;

    @Autowired
    private RentRecordRepository rentRecordRepository;

    // 1. فتح صفحة تسجيل الدفعات
    @GetMapping("/entry")
    public String showPaymentEntryPage(Model model) {
        // نجلب كل العقود النشطة لملء القائمة المنسدلة
        model.addAttribute("contracts", rentalService.getAllCombinedData());
        return "payment-entry"; // اسم ملف الـ HTML
    }

    // 2. API لجلب الدفعات الخاصة بعقد معين (للجافاسكربت)
    @GetMapping("/api/records/{contractId}")
    @ResponseBody
    public List<Map<String, Object>> getRecordsByContract(@PathVariable Long contractId) {
        List<RentRecord> records = rentRecordRepository.findByContractId(contractId);

        // تحويل البيانات لشكل بسيط يفهمه الجافاسكربت
        return records.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("dueDate", r.getDueDate().toString());
            map.put("dueAmount", r.getAmount());
            map.put("paidAmount", r.getPaidAmount() == null ? 0 : r.getPaidAmount());
            return map;
        }).collect(Collectors.toList());
    }

    // 3. حفظ أو تعديل الدفع
    @PostMapping("/save")
    public String savePayment(@RequestParam Long recordId, @RequestParam BigDecimal amount) {
        rentalService.updatePaymentAmount(recordId, amount);
        return "redirect:/payments/entry?success";
    }
}