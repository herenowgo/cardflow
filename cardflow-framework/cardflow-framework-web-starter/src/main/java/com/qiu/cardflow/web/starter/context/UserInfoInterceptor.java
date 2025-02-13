package com.qiu.cardflow.web.starter.context;


import com.qiu.cardflow.web.starter.constants.AuthConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 将用户的信息设置到线程上下文中
 */
public class UserInfoInterceptor implements HandlerInterceptor, Ordered {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取 user-id
        String userId = request.getHeader(AuthConstant.USER_ID);
        String role = request.getHeader(AuthConstant.ROLE);
        // 如果存在 user-id 字段，则存储到 ThreadLocal
        if (userId != null) {
            UserContext.setUserId(Long.parseLong(userId));
        }
        if (role != null) {
            UserContext.setUserRole(role);
        }

        // 继续请求处理
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求处理完后，清理 ThreadLocal
        UserContext.clear();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}