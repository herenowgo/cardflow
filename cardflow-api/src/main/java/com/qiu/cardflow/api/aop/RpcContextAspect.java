package com.qiu.cardflow.api.aop;

import com.qiu.cardflow.api.context.UserContext;
import org.apache.dubbo.rpc.RpcContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RpcContextAspect {

    // 使用 within() 和 + 通配符匹配所有RPC接口的子接口
    @Pointcut("within(com.qiu.cardflow..*+) && execution(* *(..)) && this(com.qiu.cardflow.common.interfaces.exception.RPC)")
    public void rpcCall() {
    }


    @Before("rpcCall()")
    public void injectUserId() {
        Long userId = UserContext.getUserId();
        if (userId != null) { // 添加userId非空判断，避免空指针异常
            RpcContext.getClientAttachment().setAttachment("user-id", userId);
        } else {
        }
    }
}