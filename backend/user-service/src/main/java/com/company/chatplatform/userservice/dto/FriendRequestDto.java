package com.company.chatplatform.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public class FriendRequestDto {

    @NotBlank(message = "Addressee User ID is required")
    private String addresseeId;

    public FriendRequestDto() {}

    public FriendRequestDto(String addresseeId) {
        this.addresseeId = addresseeId;
    }

    public String getAddresseeId() {
        return addresseeId;
    }

    public void setAddresseeId(String addresseeId) {
        this.addresseeId = addresseeId;
    }
}
