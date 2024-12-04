package com.qiu.qoj.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiu.qoj.user.model.dto.user.UserQueryRequest;
import com.qiu.qoj.common.model.entity.User;
import com.qiu.qoj.user.model.vo.LoginUserVO;
import com.qiu.qoj.user.model.vo.UserVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    @Transactional
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount      用户账户
     * @param verificationCode 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    LoginUserVO userLogin(String userAccount, String verificationCode, HttpServletRequest request);


    /**
     * 用户短信登录
     *
     * @param phone
     * @param userPassword
     * @param request
     * @return
     */
    LoginUserVO userSmsLogin(String phone, String userPassword, HttpServletRequest request);

    /**
     * 发送验证码
     *
     * @param phone
     */
    void sendVerificationCode(String phone);

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user
     * @return
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList
     * @return
     */
    List<UserVO> getUserVO(List<User> userList);


    UserVO getUserVOById(Long id);
    /**
     * 获取查询条件
     *
     * @param userQueryRequest
     * @return
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 上传头像
     *
     * @param user
     * @param filePath
     */
    void uploadAvatar(User user, String filePath, String oldUserAvatar);

}
