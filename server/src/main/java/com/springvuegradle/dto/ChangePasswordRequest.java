package com.springvuegradle.dto;

public class ChangePasswordRequest {

    private Long userId;
    private String currentPassword;
    private String newPassword;
    private String confPassword;

    public ChangePasswordRequest(Long userId, String currentPassword, String newPassword, String confPassword) {
        this.userId = userId;
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
        this.confPassword = confPassword;
    }

    // getters and setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getConfPassword() {
        return confPassword;
    }

    public void setConfPassword(String confPassword) {
        this.confPassword = confPassword;
    }
}
