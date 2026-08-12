package com.company.chatplatform.userservice.dto;

public class UserPreferenceDto {
    private String userId;
    private String theme;
    private boolean notificationsEnabled;
    private boolean soundEnabled;

    public UserPreferenceDto() {}

    public UserPreferenceDto(String userId, String theme, boolean notificationsEnabled, boolean soundEnabled) {
        this.userId = userId;
        this.theme = theme;
        this.notificationsEnabled = notificationsEnabled;
        this.soundEnabled = soundEnabled;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }
}
