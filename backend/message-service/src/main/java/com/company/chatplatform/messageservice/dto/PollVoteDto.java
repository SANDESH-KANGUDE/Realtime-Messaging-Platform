package com.company.chatplatform.messageservice.dto;

public class PollVoteDto {
    private String userId;
    private int optionIndex;

    public PollVoteDto() {}

    public PollVoteDto(String userId, int optionIndex) {
        this.userId = userId;
        this.optionIndex = optionIndex;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getOptionIndex() {
        return optionIndex;
    }

    public void setOptionIndex(int optionIndex) {
        this.optionIndex = optionIndex;
    }
}
