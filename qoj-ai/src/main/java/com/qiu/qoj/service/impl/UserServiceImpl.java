package com.qiu.qoj.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiu.qoj.config.CosClientConfig;
import com.qiu.qoj.constant.AuthConstant;
import com.qiu.qoj.constant.CommonConstant;
import com.qiu.qoj.constant.UserConstant;
import com.qiu.qoj.domain.ErrorCode;
import com.qiu.qoj.exception.BusinessException;
import com.qiu.qoj.mapper.UserMapper;
import com.qiu.qoj.model.dto.user.UserQueryRequest;
import com.qiu.qoj.model.entity.User;
import com.qiu.qoj.model.enums.UserRoleEnum;
import com.qiu.qoj.model.vo.LoginUserVO;
import com.qiu.qoj.model.vo.UserVO;
import com.qiu.qoj.service.UserService;
import com.qiu.qoj.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qiu.qoj.constant.UserConstant.*;

/**
 * 用户服务实现
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {


    private final StringRedisTemplate stringRedisTemplate;
    private final CosClientConfig cosClientConfig;
    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "qiu";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName(UserConstant.DEFAULT_USERNAME_PRIFIX + RandomUtil.randomString(4) + RandomUtil.randomInt(1000, 9999));
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    /**
     * 用户短信登录
     *
     * @param phone
     * @param verificationCode
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userSmsLogin(String phone, String verificationCode, HttpServletRequest request) {
        // 参数校验
        if (phone.length() <= 6 || !ReUtil.isMatch("^1[3-9]\\d{9}$", phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号非法");
        }
        if (verificationCode.length() != 6) throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码非法");

        // 验证手机号是否存在
        User user = lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不存在");
        }

        // 验证验证码是否正确
        String verificationCodeInRedis = stringRedisTemplate.opsForValue().get(USER_VERVIFICATION_CODE_PREFIX + phone);
        if (StrUtil.isBlank(verificationCodeInRedis)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }

        String[] split = verificationCodeInRedis.split("-");
        if (!verificationCode.equals(split[0])) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }

        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);


        return getLoginUserVO(user);
    }

    @Override
    public void sendVerificationCode(String phone) {
        // 参数校验
        if (phone.length() <= 6 || !ReUtil.isMatch("^1[3-9]\\d{9}$", phone)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号非法");
        }
        // 验证手机号是否已经存在
        User user = lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "手机号不存在");
        }
        // 同一手机号60秒内只能发送一次
        String verificationCodeInRedis = stringRedisTemplate.opsForValue().get(USER_VERVIFICATION_CODE_PREFIX + phone);
        if (verificationCodeInRedis != null) {
            String[] split = verificationCodeInRedis.split("-");
            Long timeOfSent = Long.valueOf(split[1]);
            if (System.currentTimeMillis() - timeOfSent <= 60 * 1000) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码发送过于频繁");
            }
        }
        // 发送验证码
        String verificationCode = RandomUtil.randomNumbers(6);
        log.info("验证码：{}", verificationCode);

        // 将验证码保存到redis中
        String timestamp = "-" + System.currentTimeMillis();
        verificationCode = verificationCode + timestamp;
        stringRedisTemplate.opsForValue().set(USER_VERVIFICATION_CODE_PREFIX + phone, verificationCode, 10, TimeUnit.MINUTES);

    }


    /**
     * 获取当前登录用户
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        StpUtil.checkLogin();
        // 从数据库查询

        return (User) StpUtil.getSession().get(AuthConstant.STP_MEMBER_INFO);

    }

    /**
     * 获取当前登录用户（允许未登录）
     *
     * @param request
     * @return
     */
    @Override
    public User getLoginUserPermitNull(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            return null;
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        return this.getById(userId);
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        return true;
    }

    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public void uploadAvatar(User user, String filePath, String oldUserAvatar) {
        updateById(user);
        stringRedisTemplate.opsForSet().add(USER_AVATAR_SET, filePath);
        Integer cosHostLength = cosClientConfig.getHost().length();
        String oldAvatarPath = oldUserAvatar.substring(cosHostLength + 1);
        stringRedisTemplate.opsForSet().remove(USER_AVATAR_SET, oldAvatarPath);
        stringRedisTemplate.opsForSet().add(USER_AVATAR_DB_SET, filePath);
    }
}
