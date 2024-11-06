package com.qiu.qoj.authentication;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.qiu.qoj.constant.AuthConstant;
import com.qiu.qoj.model.entity.User;
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
        User user  = (User) StpUtil.getSession().get(AuthConstant.STP_MEMBER_INFO);

        return List.of(user.getUserRole());
    }
}
