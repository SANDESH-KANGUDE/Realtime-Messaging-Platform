package com.company.chatplatform.messageservice.dto;

import jakarta.validation.constraints.NotNull;

public class VotePollRequest {
    @NotNull(message = "Option index is required")
    private Integer optionIndex;

    public VotePollRequest() {}

    public VotePollRequest(Integer optionIndex) {
        this.optionIndex = optionIndex;
    }

    public Integer getOptionIndex() {
        return optionIndex;
    }

    public void setOptionIndex(Integer optionIndex) {
        this.optionIndex = optionIndex;
    }
}
