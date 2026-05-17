package com.example.loan.common;

import java.util.UUID;

public class UserContext {

    private static final ThreadLocal<Long> userId = new ThreadLocal<>();
    private static final ThreadLocal<UUID> userUuid = new ThreadLocal<>();

    public static void set(Long id, UUID uuid) {
        userId.set(id);
        userUuid.set(uuid);
    }

    public static Long getUserId() {
        return userId.get();
    }

    public static UUID getUserUuid() {
        return userUuid.get();
    }

    public static void clear() {
        userId.remove();
        userUuid.remove();
    }
}
