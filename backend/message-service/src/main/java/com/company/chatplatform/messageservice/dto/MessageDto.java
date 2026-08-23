package com.company.chatplatform.messageservice.dto;

import java.util.List;

public class MessageDto {
    private String id;
    private String chatId;
    private String senderId;
    private String content;
    private String type;
    private String mediaUrl;
    private String replyToMessageId;
    private boolean edited;
    private boolean deleted;
    private boolean pinned;
    private String pollQuestion;
    private List<String> pollOptions;
    private List<PollVoteDto> pollVotes;
    private List<ReactionDto> reactions;
    private List<ReadReceiptDto> readReceipts;
    private List<DeliveryReceiptDto> deliveryReceipts;
    private int readCount;
    private String createdAt;
    private String updatedAt;

    public MessageDto() {}

    public MessageDto(String id, String chatId, String senderId, String content, String type, String mediaUrl, String replyToMessageId, boolean edited, boolean deleted, boolean pinned, String pollQuestion, List<String> pollOptions, List<PollVoteDto> pollVotes, List<ReactionDto> reactions, List<ReadReceiptDto> readReceipts, List<DeliveryReceiptDto> deliveryReceipts, int readCount, String createdAt, String updatedAt) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.type = type;
        this.mediaUrl = mediaUrl;
        this.replyToMessageId = replyToMessageId;
        this.edited = edited;
        this.deleted = deleted;
        this.pinned = pinned;
        this.pollQuestion = pollQuestion;
        this.pollOptions = pollOptions;
        this.pollVotes = pollVotes;
        this.reactions = reactions;
        this.readReceipts = readReceipts;
        this.deliveryReceipts = deliveryReceipts;
        this.readCount = readCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getReplyToMessageId() {
        return replyToMessageId;
    }

    public void setReplyToMessageId(String replyToMessageId) {
        this.replyToMessageId = replyToMessageId;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public String getPollQuestion() {
        return pollQuestion;
    }

    public void setPollQuestion(String pollQuestion) {
        this.pollQuestion = pollQuestion;
    }

    public List<String> getPollOptions() {
        return pollOptions;
    }

    public void setPollOptions(List<String> pollOptions) {
        this.pollOptions = pollOptions;
    }

    public List<PollVoteDto> getPollVotes() {
        return pollVotes;
    }

    public void setPollVotes(List<PollVoteDto> pollVotes) {
        this.pollVotes = pollVotes;
    }

    public List<ReactionDto> getReactions() {
        return reactions;
    }

    public void setReactions(List<ReactionDto> reactions) {
        this.reactions = reactions;
    }

    public List<ReadReceiptDto> getReadReceipts() {
        return readReceipts;
    }

    public void setReadReceipts(List<ReadReceiptDto> readReceipts) {
        this.readReceipts = readReceipts;
    }

    public List<DeliveryReceiptDto> getDeliveryReceipts() {
        return deliveryReceipts;
    }

    public void setDeliveryReceipts(List<DeliveryReceiptDto> deliveryReceipts) {
        this.deliveryReceipts = deliveryReceipts;
    }

    public int getReadCount() {
        return readCount;
    }

    public void setReadCount(int readCount) {
        this.readCount = readCount;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
