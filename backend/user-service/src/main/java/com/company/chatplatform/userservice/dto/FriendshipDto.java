package com.company.chatplatform.userservice.dto;

public class FriendshipDto {
    private String id;
    private String requesterId;
    private String addresseeId;
    private String status;
    private UserProfileDto friendProfile;
    private String createdAt;

    public FriendshipDto() {}

    public FriendshipDto(String id, String requesterId, String addresseeId, String status, UserProfileDto friendProfile, String createdAt) {
        this.id = id;
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
        this.status = status;
        this.friendProfile = friendProfile;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(String requesterId) {
        this.requesterId = requesterId;
    }

    public String getAddresseeId() {
        return addresseeId;
    }

    public void setAddresseeId(String addresseeId) {
        this.addresseeId = addresseeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserProfileDto getFriendProfile() {
        return friendProfile;
    }

    public void setFriendProfile(UserProfileDto friendProfile) {
        this.friendProfile = friendProfile;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
