package com.company.chatplatform.common.kafka.topics;

public class EventTopics {
    public static final String AUTH_USER_REGISTERED = "auth.user.registered.v1";
    public static final String USER_PROFILE_UPDATED = "user.profile.updated.v1";
    public static final String FRIEND_REQUEST_SENT = "friend.request.sent.v1";
    public static final String CHAT_CREATED = "chat.created.v1";
    public static final String GROUP_UPDATED = "group.updated.v1";
    public static final String MESSAGE_SENT = "message.sent.v1";
    public static final String MESSAGE_EDITED = "message.edited.v1";
    public static final String MESSAGE_DELETED = "message.deleted.v1";
    public static final String MEDIA_UPLOADED = "media.uploaded.v1";
    public static final String PAYMENT_COMPLETED = "payment.completed.v1";
    public static final String SUBSCRIPTION_ACTIVATED = "subscription.activated.v1";
    public static final String SUBSCRIPTION_EXPIRED = "subscription.expired.v1";
    public static final String ADMIN_USER_BANNED = "admin.user.banned.v1";

    private EventTopics() {}
}
