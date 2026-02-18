package com.thamer.Rent_System.model;

import java.time.*;
import java.math.*;
import lombok.Data;


public class ContractDisplayDTO {

	private Long contractId;
	private String tenantName;
	private LocalDate contractStart;
	private LocalDate contractEnd;
	private BigDecimal rentAmount;
	private PaymentType paymentType;
	private PropertyLocation location;
	private ContractStatus status;
	public Long getContractId() {
		return contractId;
	}
	public void setContractId(Long contractId) {
		this.contractId = contractId;
	}
	public String getTenantName() {
		return tenantName;
	}
	public void setTenantName(String tenantName) {
		this.tenantName = tenantName;
	}
	public LocalDate getContractStart() {
		return contractStart;
	}
	public void setContractStart(LocalDate contractStart) {
		this.contractStart = contractStart;
	}

	
	
	public LocalDate getContractEnd() {
		return contractEnd;
	}
	public void setContractEnd(LocalDate contractEnd) {
		this.contractEnd = contractEnd;
	}
	public BigDecimal getRentAmount() {
		return rentAmount;
	}
	public void setRentAmount(BigDecimal rentAmount) {
		this.rentAmount = rentAmount;
	}
	public PaymentType getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}
	public PropertyLocation getLocation() {
		return location;
	}
	public void setLocation(PropertyLocation location) {
		this.location = location;
	}
	public ContractStatus getStatus() {
		return status;
	}
	public void setStatus(ContractStatus status) {
		this.status = status;
	}
	
	
}
