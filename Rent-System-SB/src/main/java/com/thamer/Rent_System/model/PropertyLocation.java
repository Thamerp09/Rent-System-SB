package com.thamer.Rent_System.model;

public enum PropertyLocation {
    MAKKAH("مكة المكرمة"),
    RIYADH("الرياض");
    
    // يمكنك إضافة أي مدن أخرى هنا

    private final String arabicName;

    PropertyLocation(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}