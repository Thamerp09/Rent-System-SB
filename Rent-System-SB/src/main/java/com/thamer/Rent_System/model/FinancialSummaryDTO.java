package com.thamer.Rent_System.model;

import java.math.BigDecimal;

import lombok.*;


public class FinancialSummaryDTO {
	private BigDecimal totalExpected = BigDecimal.ZERO;
	private BigDecimal totalCollected = BigDecimal.ZERO;
	private BigDecimal totalRemaining = BigDecimal.ZERO;
	
	private BigDecimal totalDueNowPayments = BigDecimal.ZERO;
	private BigDecimal totalUpcomingPayments = BigDecimal.ZERO;
	
	
	
	public BigDecimal getTotalDueNowPayments() {
		return totalDueNowPayments;
	}
	public void setTotalDueNowPayments(BigDecimal totalDueNowPayments) {
		this.totalDueNowPayments = totalDueNowPayments;
	}
	public BigDecimal getTotalUpcomingPayments() {
		return totalUpcomingPayments;
	}
	public void setTotalUpcomingPayments(BigDecimal totalUpcomingPayments) {
		this.totalUpcomingPayments = totalUpcomingPayments;
	}
	public BigDecimal getTotalExpected() {
		return totalExpected;
	}
	public void setTotalExpected(BigDecimal totalExpected) {
		this.totalExpected = totalExpected;
	}
	public BigDecimal getTotalCollected() {
		return totalCollected;
	}
	public void setTotalCollected(BigDecimal totalCollected) {
		this.totalCollected = totalCollected;
	}
	public BigDecimal getTotalRemaining() {
		return totalRemaining;
	}
	public void setTotalRemaining(BigDecimal totalRemaining) {
		this.totalRemaining = totalRemaining;
	}

}
