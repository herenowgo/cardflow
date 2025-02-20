package com.qiu.cardflow.rpc.starter;

import org.apache.dubbo.rpc.RpcContext;


public class RPCContext {
    public static Long getUserId() {
        return Long.parseLong(RpcContext.getServerAttachment().getAttachment("user-id"));
    }

    public static String getUserRole() {
        return RpcContext.getServerAttachment().getAttachment("user-role");
    }

    public static Boolean isAdmin() {
        return "admin".equals(getUserRole());
    }
}
