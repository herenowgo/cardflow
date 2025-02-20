package com.qiu.cardflow.rpc.starter;

import org.apache.dubbo.rpc.RpcContext;


public class RPCContext {
    public static Long getUserId() {
        return Long.parseLong(RpcContext.getServerAttachment().getAttachment("user-id"));
    }
}
