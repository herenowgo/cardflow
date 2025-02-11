package com.qiu.cardflow.common.authentication;

import cn.dev33.satoken.stp.StpInterface;
import com.qiu.cardflow.common.api.UserContext;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        String role = UserContext.getUserRole();
        return List.of(role);
    }
}
