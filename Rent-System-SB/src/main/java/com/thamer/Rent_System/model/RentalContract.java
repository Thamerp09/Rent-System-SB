package com.thamer.Rent_System.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import lombok.*;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalContract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate contractStart;
    private LocalDate contractEnd;
    private BigDecimal rentAmount;

    @Enumerated(EnumType.STRING)
    private PaymentType paymentType;
 // --- أضف هذا الحقل الجديد ---
    @Enumerated(EnumType.STRING)
    private PropertyLocation location;

    public PropertyLocation getLocation() {
		return location;
	}

	public void setLocation(PropertyLocation location) {
		this.location = location;
	}

	// 🔗 ربط المستأجر
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    @JsonBackReference // <--- أضف هذا السطر هنا
	private Tenant tenant;

    // 🔗 ربط الدفعات
	@OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("contract-record") // <-- التعديل الجديد
	private List<RentRecord> rentRecords = new ArrayList<>();
    
    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public List<RentRecord> getRentRecords() {
		return rentRecords;
	}

	public void setRentRecords(List<RentRecord> rentRecords) {
		this.rentRecords = rentRecords;
	}
    

}
