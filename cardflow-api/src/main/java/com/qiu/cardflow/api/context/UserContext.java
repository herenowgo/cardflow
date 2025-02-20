package com.qiu.cardflow.api.context;

import com.qiu.cardflow.api.common.UserRoleEnum;
import com.qiu.cardflow.common.interfaces.exception.Assert;

import java.util.HashMap;
import java.util.Map;

import static com.qiu.cardflow.api.common.AuthConstant.ROLE;
import static com.qiu.cardflow.api.common.AuthConstant.USER_ID;

public class UserContext {

    private static final ThreadLocal<Map<Object, Object>> resources = new InheritableThreadLocalMap<>();

    // 获取 user-id
    public static Long getUserId() {
        return (Long) get(USER_ID);
    }

    public static String getUserRole() {
        Assert.notNull(get(ROLE), "用户角色不能为空");
        return (String) get(ROLE);
    }

    public static boolean isAdmin() {
        return UserRoleEnum.ADMIN.getValue().equals(getUserRole());
    }

    // 设置 user-id
    public static void setUserId(Long userId) {
        set(USER_ID, userId);
    }

    public static void setUserRole(String userRole) {
        set(ROLE, userRole);
    }

    // 清理 user-id
    public static void clear() {
        resources.remove();
    }


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
