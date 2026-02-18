package com.thamer.Rent_System.model;

public enum FileCategory {
    RENT("إيجارات"),
    DOCUMENT("وثائق");

    private final String arabicName;

    FileCategory(String arabicName) {
        this.arabicName = arabicName;
    }

    public String getArabicName() {
        return arabicName;
    }
}