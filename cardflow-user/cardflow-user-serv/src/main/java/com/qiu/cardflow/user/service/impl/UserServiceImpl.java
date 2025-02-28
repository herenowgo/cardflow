package com.qiu.cardflow.user.service.impl;

import cn.dev33.satoken.stp.SaLoginConfig;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiu.cardflow.common.api.UserContext;
import com.qiu.cardflow.common.constant.AuthConstant;
import com.qiu.cardflow.common.constant.CommonConstant;
import com.qiu.cardflow.common.constant.UserConstant;
import com.qiu.cardflow.common.exception.Asserts;
import com.qiu.cardflow.common.model.entity.User;
import com.qiu.cardflow.user.config.CosClientConfig;
import com.qiu.cardflow.user.mapper.UserMapper;
import com.qiu.cardflow.user.model.dto.user.UserQueryRequest;
import com.qiu.cardflow.user.model.enums.UserRoleEnum;
import com.qiu.cardflow.user.model.vo.LoginUserVO;
import com.qiu.cardflow.user.model.vo.UserVO;
import com.qiu.cardflow.user.service.UserService;
import com.qiu.cardflow.user.utils.SqlUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.qiu.cardflow.common.constant.UserConstant.USER_VERVIFICATION_CODE_PREFIX;
import static com.qiu.cardflow.user.constant.UserConstant.USER_AVATAR_DB_SET;
import static com.qiu.cardflow.user.constant.UserConstant.USER_AVATAR_SET;


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
        checkUserRegisterInfo(userAccount, userPassword, checkPassword);
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                Asserts.fail("账号重复");
            }
            // 2. 加密
            String encryptPassword = BCrypt.hashpw(userPassword);
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserName(UserConstant.DEFAULT_USERNAME_PRIFIX + RandomUtil.randomString(4) + RandomUtil.randomInt(1000, 9999));
            boolean saveResult = this.save(user);
            if (!saveResult) {
                Asserts.fail("注册失败，系统错误");
            }
            return user.getId();
        }
    }

    private void checkUserRegisterInfo(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword, checkPassword)) {
            Asserts.fail("验证码错误");
        }
        if (userAccount.length() < 4) {
            Asserts.fail("用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            Asserts.fail("用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            Asserts.fail("两次输入的密码不一致");
        }
    }


    @Override
    public LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        checkUserLogin(userAccount, userPassword);
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            Asserts.fail("用户不存在或密码错误");
        }

        boolean passwordCorrect = BCrypt.checkpw(userPassword, user.getUserPassword());
        if (!passwordCorrect) {
            Asserts.fail("用户不存在或密码错误");
        }
        // 登录
        StpUtil.login(user.getId(), SaLoginConfig.setExtra(AuthConstant.ROLE, user.getUserRole()).setTimeout(2592000));


//        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    private static void checkUserLogin(String userAccount, String userPassword) {
        // 1. 校验
        if (StrUtil.hasBlank(userAccount, userPassword)) {
            Asserts.fail("参数为空");
        }
        if (userAccount.length() < 4) {
            Asserts.fail("账号错误");
        }
        if (userPassword.length() < 8) {
            Asserts.fail("密码错误");
        }
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
            Asserts.fail("手机号非法");
        }
        if (verificationCode.length() != 6) Asserts.fail("验证码非法");

        // 验证手机号是否存在
        User user = lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            Asserts.fail("手机号不存在");
        }

        // 验证验证码是否正确
        String verificationCodeInRedis = stringRedisTemplate.opsForValue().get(USER_VERVIFICATION_CODE_PREFIX + phone);
        if (StrUtil.isBlank(verificationCodeInRedis)) {
            Asserts.fail("验证码错误");
        }

        String[] split = verificationCodeInRedis.split("-");
        if (!verificationCode.equals(split[0])) {
            Asserts.fail("验证码错误");
        }

        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);


        return getLoginUserVO(user);
    }

    @Override
    public void sendVerificationCode(String phone) {
        // 参数校验
        if (phone.length() <= 6 || !ReUtil.isMatch("^1[3-9]\\d{9}$", phone)) {
            Asserts.fail("手机号非法");
        }
        // 验证手机号是否已经存在
        User user = lambdaQuery().eq(User::getPhone, phone).one();
        if (user == null) {
            Asserts.fail("手机号不存在");
        }
        // 同一手机号60秒内只能发送一次
        String verificationCodeInRedis = stringRedisTemplate.opsForValue().get(USER_VERVIFICATION_CODE_PREFIX + phone);
        if (verificationCodeInRedis != null) {
            String[] split = verificationCodeInRedis.split("-");
            Long timeOfSent = Long.valueOf(split[1]);
            if (System.currentTimeMillis() - timeOfSent <= 60 * 1000) {
                Asserts.fail("验证码发送过于频繁");
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
//        StpUtil.checkLogin();
        User user = getById(UserContext.getUserId());
        return user;
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
            Asserts.fail("未登录");
        }
        // 移除登录态
//        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
        StpUtil.logout();
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
    public UserVO getUserVOById(Long id) {
        User user = getById(id);
        return getUserVO(user);
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            Asserts.fail("请求参数为空");
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
        queryWrapper.eq(StrUtil.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StrUtil.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
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
