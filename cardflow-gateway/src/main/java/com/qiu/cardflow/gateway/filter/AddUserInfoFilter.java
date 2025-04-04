package com.qiu.cardflow.gateway.filter;

import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import lombok.extern.slf4j.Slf4j;

import com.qiu.cardflow.gateway.constant.AuthConstant;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 解析 JWT，将用户信息添加到请求头中
 */
@Configuration
@Slf4j
public class AddUserInfoFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        try {
            if (request.getCookies().containsKey(AuthConstant.AUTHORIZATION_HEADER)) {
                HttpCookie cookie = request.getCookies().getFirst(AuthConstant.AUTHORIZATION_HEADER);
                String authorization = cookie.getValue();
                JWT jwt = JWTUtil.parseToken(authorization);
                String loginId = jwt.getPayload(AuthConstant.JWT_PAYLOAD_LOGIN_ID).toString();
                String role = jwt.getPayload(AuthConstant.ROLE).toString();

                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(AuthConstant.USER_ID, loginId)
                        .header(AuthConstant.ROLE, role)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }
        } catch (Exception e) {
            // 记录异常但不阻止请求继续传递
            log.error("JWT解析失败", e);
        }

        return chain.filter(exchange);
    }
}
