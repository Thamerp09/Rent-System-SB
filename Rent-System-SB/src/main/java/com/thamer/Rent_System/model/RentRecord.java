package com.thamer.Rent_System.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.thamer.Rent_System.model.*;

import jakarta.persistence.Transient;


@Entity
@AllArgsConstructor
@Builder
public class RentRecord {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "contract_id")
	@JsonBackReference("contract-record") // <-- التعديل الجديد
	private RentalContract contract;

	private LocalDate dueDate; // متى لازم يدفع

	private BigDecimal amount; // كم المبلغ

	private boolean paid = false; // هل تم الدفع
	// أضف هذا الحقل الجديد
	private BigDecimal paidAmount; // المبلغ الذي تم دفعه فعلياً




	@Transient
	private boolean isProjected = false; // هل هذا سجل متوقع؟

	public boolean isProjected() {
		return isProjected;
	}

	public void setProjected(boolean projected) {
		isProjected = projected;
	}


	
	// عدل الـ Getter و Setter
	public BigDecimal getPaidAmount() {
		return paidAmount;
	}

	public void setPaidAmount(BigDecimal paidAmount) {
		this.paidAmount = paidAmount;
	}

	public RentRecord(RentalContract contract, LocalDate dueDate, BigDecimal amount) {
		this.contract = contract;
		this.dueDate = dueDate;
		this.amount = amount;
		this.paid = false;
	}

	public RentRecord() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public RentalContract getContract() {
		return contract;
	}

	public void setContract(RentalContract contract) {
		this.contract = contract;
	}

	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public boolean isPaid() {
		return paid;
	}

	public void setPaid(boolean paid) {
		this.paid = paid;
	}

}
