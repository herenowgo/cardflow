package com.qiu.qoj.ai.constant;

/**
 * 用户常量
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login";

    //  region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    /**
     * 被封号
     */
    String BAN_ROLE = "ban";

    String DEFAULT_USERNAME_PRIFIX = "用户";

    String USER_VERVIFICATION_CODE_PREFIX = "user:code:";


    // endregion

    String USER_AVATAR_SET = "user:avatar";
    String USER_AVATAR_DB_SET = "user:avatar:db";


}
