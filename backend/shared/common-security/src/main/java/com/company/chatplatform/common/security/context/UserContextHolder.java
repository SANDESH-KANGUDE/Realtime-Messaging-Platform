package com.company.chatplatform.common.security.context;

public class UserContextHolder {

    private static final ThreadLocal<UserContext> CONTEXT = new ThreadLocal<>();

    private UserContextHolder() {}

    public static void setContext(UserContext context) {
        CONTEXT.set(context);
    }

    public static UserContext getContext() {
        return CONTEXT.get();
    }

    public static String getUserId() {
        UserContext context = getContext();
        return context != null ? context.userId() : null;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public record UserContext(String userId, String email, String role) {}
}
