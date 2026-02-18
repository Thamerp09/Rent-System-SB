package com.thamer.Rent_System.model;
import com.thamer.Rent_System.model.*;

import lombok.*;

@Getter
@Setter
@Data
public class TenantContractForm {
    private Tenant tenant;
    private RentalContract contract;
    private RentRecord record;
    private ContractStatus contractStatus;

    public TenantContractForm() {
        this.tenant = new Tenant();
        this.contract = new RentalContract();
        this.record = new RentRecord(contract, null, null);
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public RentalContract getContract() {
        return contract;
    }

    public void setContract(RentalContract contract) {
        this.contract = contract;
    }

	public void setRecord(RentRecord record2) {
		// TODO Auto-generated method stub
		
	}

	public ContractStatus getContractStatus() {
		return contractStatus;
	}

	public void setContractStatus(ContractStatus contractStatus) {
		this.contractStatus = contractStatus;
	}

	public RentRecord getRecord() {
		return record;
	}

//    public RentRecord getRecord() {
//        return record;
//    }
//
//    public void setRecord(RentRecord record) {
//        this.record = record;
//    }
}
