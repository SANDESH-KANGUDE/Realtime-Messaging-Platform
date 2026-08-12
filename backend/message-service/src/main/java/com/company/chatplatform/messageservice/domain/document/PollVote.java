package com.company.chatplatform.messageservice.domain.document;

public class PollVote {
    private String userId;
    private int optionIndex;

    public PollVote() {}

    public PollVote(String userId, int optionIndex) {
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
