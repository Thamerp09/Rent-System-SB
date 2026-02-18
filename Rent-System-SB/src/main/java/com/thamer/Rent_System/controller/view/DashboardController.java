package com.thamer.Rent_System.controller.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.thamer.Rent_System.service.*;
import com.thamer.Rent_System.model.*;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.util.*;
@Controller
public class DashboardController {

	@Autowired
	private RentalManagementService service;
	
	@GetMapping("/dashboard")
	public String showDashboard(Model model) {
		
		// 1. احصل على الملخص المالي وأضفه إلى الموديل
        FinancialSummaryDTO summary = service.getOverallFinancialSummary();
        model.addAttribute("financialSummary", summary);
        
        // 2. احصل على الدفعات المستحقة حالياً وأضفها
        List<RentRecord> duePayments = service.getDueNowPayments();
        model.addAttribute("dueNowPayments", duePayments);
        
     // 3. احصل على الدفعات القادمة وأضفها
        List<RentRecord> upcomingPayments = service.getUpcomingPayments();
        model.addAttribute("upcomingPayments", upcomingPayments);
        
        Map<String, BigDecimal> monthlyRevenue = service.getMonthlyExpectedRevenueForCurrentYear();
        model.addAttribute("monthlyRevenueData", monthlyRevenue);

        
        model.addAttribute("currentPage", "dashboard");
		return "dashboard";
	}
}
