package com.qiu.qoj.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiu.qoj.config.CosClientConfig;
import com.qiu.qoj.constant.UserConstant;
import com.qiu.qoj.domain.BaseResponse;
import com.qiu.qoj.domain.DeleteRequest;
import com.qiu.qoj.exception.Asserts;
import com.qiu.qoj.model.dto.file.UploadFileRequest;
import com.qiu.qoj.model.dto.user.*;
import com.qiu.qoj.model.entity.User;
import com.qiu.qoj.model.vo.LoginUserVO;
import com.qiu.qoj.model.vo.UserVO;
import com.qiu.qoj.service.FileService;
import com.qiu.qoj.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private FileService fileService;

    @Resource
    private CosClientConfig cosClientConfig;


    // region 登录相关

    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            Asserts.fail("请求参数为空");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return BaseResponse.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            Asserts.fail("请求参数为空");
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            Asserts.fail("请求参数为空");
        }
        LoginUserVO loginUserVO = userService.userLogin(userAccount, userPassword, request);
        return BaseResponse.success(loginUserVO);
    }

    /**
     * 用户短信登录
     *
     * @param userSmsLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/smsLogin")
    public BaseResponse<LoginUserVO> userSmsLogin(@RequestBody UserSmsLoginRequest userSmsLoginRequest, HttpServletRequest request) {
        if (userSmsLoginRequest == null) {
            Asserts.fail("请求参数为空");
        }
        String phone = userSmsLoginRequest.getPhone();
        String verificationCode = userSmsLoginRequest.getVerificationCode();
        if (StrUtil.hasBlank(phone, verificationCode)) {
            Asserts.fail("请求参数为空");
        }
        LoginUserVO loginUserVO = userService.userSmsLogin(phone, verificationCode, request);
        return BaseResponse.success(loginUserVO);
    }

    /**
     * 发送验证码
     *
     * @param phone
     * @return
     */
    @GetMapping("/sendVerificationCode")
    public BaseResponse<String> doSendVerificationCode(String phone) {
        if (StrUtil.isBlank(phone)) {
            Asserts.fail("请求参数为空");
        }
        userService.sendVerificationCode(phone);
        return BaseResponse.success("ok");
    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            Asserts.fail("请求参数为空");
        }
        boolean result = userService.userLogout(request);
        return BaseResponse.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return BaseResponse.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            Asserts.fail("请求参数为空");
        }
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        boolean result = userService.save(user);

        Asserts.failIf(!result, "创建用户失败");
        return BaseResponse.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            Asserts.fail("请求参数错误");
        }

        StpUtil.logout(deleteRequest.getId());
        boolean b = userService.removeById(deleteRequest.getId());
        return BaseResponse.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            Asserts.fail("请求参数错误");
        }
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        Asserts.failIf(!result, "更新用户失败");
        StpUtil.logout(user.getId());
        return BaseResponse.success(true);
    }

    /**
     * 根据 id 获取用户（仅管理员）
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        if (id <= 0) {
            Asserts.fail("请求参数错误");
        }
        User user = userService.getById(id);
        Asserts.failIf(user == null, "用户不存在");
        return BaseResponse.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return BaseResponse.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    @SaCheckRole(UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return BaseResponse.success(userPage);
    }

    /**
     * 分页获取用户封装列表
     *
     * @param userQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        if (userQueryRequest == null) {
            Asserts.fail("请求参数为空");
        }
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        // 限制爬虫
        Asserts.failIf(size > 20, "请求参数错误");
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, size, userPage.getTotal());
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return BaseResponse.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        if (userUpdateMyRequest == null) {
            Asserts.fail("请求参数为空");
        }
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());
        boolean result = userService.updateById(user);
        Asserts.failIf(!result, "更新失败");
        return BaseResponse.success(true);
    }


    /**
     * 上传头像
     *
     * @param multipartFile
     * @param uploadFileRequest
     * @param request
     * @return
     */
    @PostMapping("/uploadAvatar")
    public BaseResponse<String> uploadAvatar(@RequestPart("file") MultipartFile multipartFile,
                                             UploadFileRequest uploadFileRequest, HttpServletRequest request) {
        String filePath = fileService.uploadFile(multipartFile, uploadFileRequest, request);
        String avatarPath = cosClientConfig.getHost() + "/" + filePath;
        User loginUser = userService.getLoginUser(request);
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserAvatar(avatarPath);
        String oldUserAvatar = loginUser.getUserAvatar();
        userService.uploadAvatar(user, filePath, oldUserAvatar);
        return BaseResponse.success(avatarPath);
    }

    /**
     * 下载头像
     *
     * @param response
     * @param request
     */
    @GetMapping("downloadAvatar")
    public void downloadAvatar(HttpServletResponse response, HttpServletRequest request) {

        User loginUser = userService.getLoginUser(request);
        fileService.downloadFile(loginUser.getUserAvatar().replace(cosClientConfig.getHost(), "").substring(1), response);
    }
}
