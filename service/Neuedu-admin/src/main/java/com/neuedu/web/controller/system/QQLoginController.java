package com.neuedu.web.controller.system;

import com.neuedu.common.constant.Constants;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.common.utils.SecurityUtils;
import com.neuedu.system.service.ISysUserService;
import com.neuedu.web.service.TokenService;
import com.neuedu.web.service.SysPermissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * QQ登录控制器
 */
@RestController
@RequestMapping("/qq")
public class QQLoginController {

    private static final Logger log = LoggerFactory.getLogger(QQLoginController.class);

    // QQ登录API配置
    private static final String QQ_LOGIN_URL = "https://qq.wch666.com/api/qq.php";
    private static final String QQ_USER_INFO_URL = "https://qq.wch666.com/api/get_user_info.php";
    private static final String QQ_TOKEN = "4922a38240ef9fd21bfc5dd47bff232e";

    // 前端登录成功后的回调页面由部署配置提供，默认值仅用于本地开发。
    @Value("${shopping.qq.frontend-callback-url}")
    private String frontendCallbackUrl;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SysPermissionService permissionService;

    /**
     * 获取QQ登录跳转URL
     */
    @GetMapping("/login-url")
    public AjaxResult getLoginUrl(@RequestParam(value = "display", defaultValue = "pc") String display) {
        String loginUrl = QQ_LOGIN_URL + "?token=" + QQ_TOKEN + "&msg=user_login&display=" + display;
        return AjaxResult.success("获取成功", loginUrl);
    }

    /**
     * QQ登录回调接口
     * QQ授权成功后会重定向到此接口
     */
    @GetMapping("/callback")
    public void callback(@RequestParam("code") String code,
                         @RequestParam(value = "msg", required = false) String msg,
                         HttpServletResponse response) throws IOException {
        log.info("收到QQ登录回调");

        try {
            // 1. 使用code获取QQ用户信息
            RestTemplate restTemplate = new RestTemplate();
            String userInfoUrl = QQ_USER_INFO_URL + "?code=" + code;
            
            // 获取原始字符串响应
            String rawResponse = restTemplate.getForObject(userInfoUrl, String.class);
            // 手动解析JSON
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> qqUserInfo = objectMapper.readValue(rawResponse, Map.class);
            
            if (qqUserInfo == null) {
                log.error("获取QQ用户信息失败: 返回null");
                response.sendRedirect(frontendCallbackUrl + "?error=获取QQ用户信息失败");
                return;
            }

            // 2. 解析用户信息 - 根据实际返回结构调整
            String qqOpenid = qqUserInfo.get("open_id") != null ? qqUserInfo.get("open_id").toString() : null;
            String nickname = qqUserInfo.get("nickname") != null ? qqUserInfo.get("nickname").toString() : "QQ用户";
            // 头像URL可能过长，限制长度或使用默认头像
            String avatarUrl = qqUserInfo.get("figureurl_qq_2") != null ? qqUserInfo.get("figureurl_qq_2").toString() : 
                           (qqUserInfo.get("figureurl_qq_1") != null ? qqUserInfo.get("figureurl_qq_1").toString() : "");
            String avatar = avatarUrl.length() > 200 ? "" : avatarUrl;

            if (qqOpenid == null || qqOpenid.isEmpty()) {
                response.sendRedirect(frontendCallbackUrl + "?error=无法获取QQ标识");
                return;
            }

            log.info("QQ用户身份校验完成");

            // 3. 查找或创建用户
            SysUser user = userService.selectUserByQqOpenid(qqOpenid);
            
            if (user == null) {
                // 用户不存在，创建新用户
                user = new SysUser();
                user.setQqOpenid(qqOpenid);
                user.setUserName("qq_" + qqOpenid.substring(0, Math.min(16, qqOpenid.length())));
                user.setNickName(nickname);
                user.setAvatar(avatar);
                user.setPassword(SecurityUtils.encryptPassword("qq_default_" + qqOpenid));
                user.setUserType(0); // 普通用户
                user.setStatus("0"); // 正常状态
                user.setDelFlag("0");
                
                // 分配默认角色（普通用户角色）
                Long[] roleIds = new Long[]{2L}; // 假设角色ID 2 是普通用户
                user.setRoleIds(roleIds);
                
                userService.insertUser(user);
                log.info("已创建QQ登录用户");
                
                // 重新查询获取完整用户信息
                user = userService.selectUserByQqOpenid(qqOpenid);
            } else {
                // 用户已存在，更新头像和昵称
                user.setNickName(nickname);
                user.setAvatar(avatar);
                userService.updateUserProfile(user);
                log.info("已更新QQ登录用户资料");
            }

            // 4. 生成登录token
            Set<String> permissions = permissionService.getMenuPermission(user);
            LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, permissions);
            String token = tokenService.createToken(loginUser);

            log.info("QQ登录成功");

            // 5. 重定向到前端，携带token
            response.sendRedirect(frontendCallbackUrl + "?token=" + token);

        } catch (Exception e) {
            log.error("QQ登录处理失败，异常类型={}", e.getClass().getSimpleName());
            response.sendRedirect(frontendCallbackUrl + "?error=QQ登录失败，请稍后重试");
        }
    }

    /**
     * 通过token直接登录（前端调用）
     */
    @PostMapping("/login-by-token")
    public AjaxResult loginByToken(@RequestBody Map<String, String> params) {
        String token = params.get("token");
        if (token == null || token.isEmpty()) {
            return AjaxResult.error("token不能为空");
        }
        
        AjaxResult ajax = AjaxResult.success("QQ登录成功");
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }
}
