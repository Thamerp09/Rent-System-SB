package com.thamer.Rent_System.model; // <--- حزمة جديدة لـ DTOs

// DTO for returning logged-in user info
public class UserInfoDTO { // <--- يجب أن تكون public
    private String username;
    private String displayName;

    public UserInfoDTO(String username, String displayName) {
        this.username = username;
        this.displayName = displayName;
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getDisplayName() {
        return displayName;
    }
}