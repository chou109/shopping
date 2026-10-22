package com.neuedu.web.service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.neuedu.common.constant.UserTypeConstants;
import com.neuedu.web.manager.AsyncManager;
import com.neuedu.web.manager.factory.AsyncFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.neuedu.common.constant.CacheConstants;
import com.neuedu.common.constant.Constants;
import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.entity.SysRole;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.common.core.redis.RedisCache;
import com.neuedu.common.exception.ServiceException;
import com.neuedu.common.exception.user.CaptchaException;
import com.neuedu.common.exception.user.CaptchaExpireException;
import com.neuedu.common.exception.user.UserPasswordNotMatchException;
import com.neuedu.common.utils.DateUtils;
import com.neuedu.common.utils.MessageUtils;
import com.neuedu.common.utils.ServletUtils;
import com.neuedu.common.utils.StringUtils;
import com.neuedu.common.utils.ip.IpUtils;
import com.neuedu.framework.security.context.AuthenticationContextHolder;
import com.neuedu.system.service.ISysConfigService;
import com.neuedu.system.service.ISysUserService;

import java.util.HashSet;
import java.util.Set;

/**
 * 登录校验方法
 *
 * @author neuedu
 */
@Component
public class SysLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 登录验证
     *
     * @param username 用户名
     * @param password 密码
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String uuid)
    {
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        // 验证码开关
        if (captchaEnabled)
        {
            validateCaptcha(username, code, uuid);
        }
        // 用户验证
        Authentication authentication = null;
        try
        {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e)
        {
            if (e instanceof BadCredentialsException)
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            }
            else
            {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        }
        finally
        {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();


        String clientType = ServletUtils.getRequest().getHeader("X-Client-Type");
        SysUser user = loginUser.getUser();

//        SysUser type = loginUser.getUserType()l

        boolean isAdmin = isAdminAccount(user);

        if ("admin".equals(clientType)) {
            // 管理端同时承载商家和管理员页面，角色权限仍由动态菜单及后端接口校验。
            if (!isAdmin && !isMerchantAccount(user)) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(
                        username, Constants.LOGIN_FAIL, "无商家或管理员角色的账号尝试登录管理端"));
                throw new ServiceException("该账号没有管理后台权限");
            }

        } else if ("user".equals(clientType)) {
            // 消费者端登录，禁止商家和管理员账号。
            if (isAdmin || isMerchantAccount(user)) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(
                        username, Constants.LOGIN_FAIL, "商家或管理员账号尝试登录用户端"));
                throw new ServiceException("登录失败");
            }

        } else {
            // 没有客户端类型头，记录警告
        }

        recordLoginInfo(loginUser.getUserId());
        // 生成token
        return tokenService.createToken(loginUser);
    }

    static boolean isAdminAccount(SysUser user)
    {
        return user != null && (SysUser.isAdmin(user.getUserId())
                || UserTypeConstants.isAdmin(user.getUserType()));
    }

    static boolean isMerchantAccount(SysUser user)
    {
        if (user == null || user.getRoles() == null)
        {
            return false;
        }
        return user.getRoles().stream().map(SysRole::getRoleKey).anyMatch("shang"::equals);
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId)
    {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }
}
