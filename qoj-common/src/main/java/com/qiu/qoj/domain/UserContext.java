package com.qiu.qoj.domain;

public class UserContext {
    private static final ThreadLocal<Long> userIdThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> userRoleThreadLocal = new ThreadLocal<>();

    // 获取 user-id
    public static Long getUserId() {
        return userIdThreadLocal.get();
    }

    public static String getUserRole() {
        return userRoleThreadLocal.get();
    }


    // 设置 user-id
    public static void setUserId(Long userId) {
        userIdThreadLocal.set(userId);
    }

    public static void setUserRole(String userRole) {
        userRoleThreadLocal.set(userRole);
    }

    // 清理 user-id
    public static void clear() {
        userIdThreadLocal.remove();
        userRoleThreadLocal.remove();
    }
}
