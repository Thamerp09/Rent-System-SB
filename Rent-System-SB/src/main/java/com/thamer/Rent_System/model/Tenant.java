package com.thamer.Rent_System.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.*;

import java.util.*;

import com.fasterxml.jackson.annotation.JsonManagedReference;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Tenant {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private String name;
	
	
	
	
	@OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference // <--- أضف هذا السطر هنا	
	private List<RentalContract> rentalContract = new ArrayList<>();

	
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public List<RentalContract> getRentalContract() {
		return rentalContract;
	}


	public void setRentalContract(List<RentalContract> rentalContract) {
		this.rentalContract = rentalContract;
	}
	
	
}
