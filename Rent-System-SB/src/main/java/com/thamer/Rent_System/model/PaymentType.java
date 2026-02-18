package com.thamer.Rent_System.model;

public enum PaymentType {
    // القيم مع الأسماء العربية
    MONTHLY("شهري"),
    QUARTERLY("ربع سنوي"),
    SEMI_ANNUALLY("نصف سنوي"),
    ANNUALLY("سنوي");

    private final String displayName;

    PaymentType(String displayName) {
        this.displayName = displayName;
    }

    // تأكد من أن هذه الدالة public
    public String getDisplayName() {
        return displayName;
    }
}