package com.qiu.cardflow.common.api;

import com.qiu.cardflow.common.enums.UserRoleEnum;

import java.util.HashMap;
import java.util.Map;

public class UserContext {

    private static final ThreadLocal<Map<Object, Object>> resources = new InheritableThreadLocalMap<>();
    public static final String USER_ID = "userId";
    public static final String USER_ROLE = "userRole";


    public static void set(Object key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
        if (value == null) {
            resources.get().remove(key);
        }
        resources.get().put(key, value);
    }

    public static Object get(Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
        return resources.get().get(key);
    }

    // 获取 user-id
    public static Long getUserId() {
        return (Long) get(USER_ID);
    }

    public static String getUserRole() {
        return (String) get(USER_ROLE);
    }

    public static boolean isAdmin() {
        return UserRoleEnum.ADMIN.getValue().equals(getUserRole());
    }

    // 设置 user-id
    public static void setUserId(Long userId) {
        set(USER_ID, userId);
    }

    public static void setUserRole(String userRole) {
        set(USER_ROLE, userRole);
    }

    // 清理 user-id
    public static void clear() {
        resources.remove();
    }


    //实现父子线程之间的线程本地变量传递
    //A-->threadLocal ("userId",1001)
    //A-->new Thread(B)-->B线程属于A线程的子线程，threadLocal get("userId")
    private static final class InheritableThreadLocalMap<T extends Map<Object, Object>> extends InheritableThreadLocal<Map<Object, Object>> {

        @Override
        protected Map<Object, Object> initialValue() {
            return new HashMap();
        }

        @Override
        protected Map<Object, Object> childValue(Map<Object, Object> parentValue) {
            if (parentValue != null) {
                return (Map<Object, Object>) ((HashMap<Object, Object>) parentValue).clone();
            } else {
                return null;
            }
        }
    }
}
