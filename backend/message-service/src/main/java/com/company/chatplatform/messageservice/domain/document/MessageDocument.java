package com.company.chatplatform.messageservice.domain.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "messages")
public class MessageDocument {

    @Id
    private String id;

    @Indexed
    private String chatId;

    @Indexed
    private String senderId;

    private String content;
    private String type = "TEXT"; // TEXT, IMAGE, FILE, SYSTEM, POLL
    private String mediaUrl;
    private String replyToMessageId;
    private boolean edited = false;
    private boolean deleted = false;
    private boolean pinned = false;

    private String pollQuestion;
    private List<String> pollOptions = new ArrayList<>();
    private List<PollVote> pollVotes = new ArrayList<>();

    private List<ReactionDocument> reactions = new ArrayList<>();
    private List<ReadReceiptDocument> readReceipts = new ArrayList<>();

    @Indexed
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public MessageDocument() {}

    public MessageDocument(String id, String chatId, String senderId, String content, String type, String mediaUrl, String replyToMessageId) {
        this.id = id;
        this.chatId = chatId;
        this.senderId = senderId;
        this.content = content;
        this.type = type != null ? type : "TEXT";
        this.mediaUrl = mediaUrl;
        this.replyToMessageId = replyToMessageId;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getChatId() {
        return chatId;
    }

    public String getSenderId() {
        return senderId;
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

    public List<ReactionDocument> getReactions() {
        return reactions;
    }

    public void setReactions(List<ReactionDocument> reactions) {
        this.reactions = reactions;
    }

    public List<ReadReceiptDocument> getReadReceipts() {
        return readReceipts;
    }

    public void setReadReceipts(List<ReadReceiptDocument> readReceipts) {
        this.readReceipts = readReceipts;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
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

    public List<PollVote> getPollVotes() {
        return pollVotes;
    }

    public void setPollVotes(List<PollVote> pollVotes) {
        this.pollVotes = pollVotes;
    }
}
