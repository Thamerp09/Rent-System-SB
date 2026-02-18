package com.thamer.Rent_System.model;




public enum ContractStatus {

	ACTIVE("نشط"),
	EXPIRING_SOON("قارب على الانتهاء"),
	EXPIRED("منتهي");
	
	private final String displayName;
	
	ContractStatus (String displayName){
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
}
