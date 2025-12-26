package com.neuedu.web.controller.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.neuedu.common.annotation.Log;
import com.neuedu.common.constant.UserConstants;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.core.domain.entity.SysDept;
import com.neuedu.common.core.domain.entity.SysRole;
import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.page.TableDataInfo;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.common.utils.SecurityUtils;
import com.neuedu.common.utils.StringUtils;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.system.service.ISysDeptService;
import com.neuedu.system.service.ISysPostService;
import com.neuedu.system.service.ISysRoleService;
import com.neuedu.system.service.ISysUserService;

/**
 * 用户信息
 * 
 * @author neuedu
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysPostService postService;

    /**
     * 获取用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }


    @Log(title = "修改密码", businessType = BusinessType.UPDATE)
    @PutMapping("/password")
    public AjaxResult updatePassword(@RequestBody Map<String, String> params)
    {
        System.out.println("=== 修改密码 ===");

        try {
            Long userId = SecurityUtils.getUserId();
            if (userId == null) {
                return error("用户未登录");
            }

            // 获取参数
            String oldPassword = params.get("oldPassword");
            String newPassword = params.get("newPassword");
            String confirmPassword = params.get("confirmPassword");

            System.out.println("参数检查:");
            System.out.println("- 原密码: " + (StringUtils.isNotEmpty(oldPassword) ? "已提供" : "为空"));
            System.out.println("- 新密码: " + (StringUtils.isNotEmpty(newPassword) ? "已提供" : "为空"));
            System.out.println("- 确认密码: " + (StringUtils.isNotEmpty(confirmPassword) ? "已提供" : "为空"));

            // 参数验证
            if (StringUtils.isEmpty(oldPassword)) {
                return error("原密码不能为空");
            }
            if (StringUtils.isEmpty(newPassword)) {
                return error("新密码不能为空");
            }
            if (StringUtils.isEmpty(confirmPassword)) {
                return error("确认密码不能为空");
            }
            if (!newPassword.equals(confirmPassword)) {
                return error("两次输入的新密码不一致");
            }
            if (newPassword.length() < 6) {
                return error("密码长度不能少于6位");
            }
            if (oldPassword.equals(newPassword)) {
                return error("新密码不能与原密码相同");
            }

            // 获取用户信息
            SysUser user = userService.selectUserById(userId);
            if (user == null) {
                return error("用户不存在");
            }

            System.out.println("开始验证原密码...");
            System.out.println("用户当前密码hash: " + user.getPassword());

            // 验证原密码
            boolean passwordMatches = SecurityUtils.matchesPassword(oldPassword, user.getPassword());
            System.out.println("密码验证结果: " + passwordMatches);

            if (!passwordMatches) {
                return error("原密码错误");
            }

            // 加密新密码
            String encryptedPassword = SecurityUtils.encryptPassword(newPassword);
            System.out.println("加密后的新密码: " + encryptedPassword);

            // 更新密码
            user.setPassword(encryptedPassword);
            user.setUpdateBy(SecurityUtils.getUsername());

            int rows = userService.resetPwd(user);
            System.out.println("更新密码影响行数: " + rows);

            if (rows > 0) {
                return success("密码修改成功");
            } else {
                return error("密码修改失败");
            }

        } catch (Exception e) {
            System.out.println("修改密码异常: " + e.getMessage());
            e.printStackTrace();
            return error("修改密码失败: " + e.getMessage());
        }
    }


    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysUser user)
    {
        List<SysUser> list = userService.selectUserList(user);
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.exportExcel(response, list, "用户数据");
    }

    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('system:user:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        List<SysUser> userList = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = userService.importUser(userList, updateSupport, operName);
        return success(message);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
        util.importTemplateExcel(response, "用户数据");
    }

    /**
     * 根据用户编号获取详细信息
     */
//    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = { "/", "/{userId}" })
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId)
    {
        userService.checkUserDataScope(userId);
        AjaxResult ajax = AjaxResult.success();
        List<SysRole> roles = roleService.selectRoleAll();
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.selectPostAll());
        if (StringUtils.isNotNull(userId))
        {
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));
        }
        return ajax;
    }

    /**
     * 获取当前登录用户信息（用户端专用）
     */
    @GetMapping("/current")
    public AjaxResult getCurrentUserInfo()
    {
        System.out.println("=== 开始获取当前用户信息 ===");

        // 获取当前用户ID
        Long currentUserId = getUserId();
        System.out.println("获取到的用户ID: " + currentUserId);

        if (currentUserId == null) {
            System.out.println("错误：用户未登录，getUserId() 返回 null");
            return error("用户未登录");
        }

        // 获取当前用户名（用于调试）
        String username = getUsername();
        System.out.println("当前用户名: " + username);

        // 查询用户信息
        System.out.println("开始查询用户信息，用户ID: " + currentUserId);
        SysUser sysUser = userService.selectUserById(currentUserId);

        if (sysUser == null) {
            System.out.println("错误：用户不存在，用户ID: " + currentUserId);
            return error("用户不存在");
        }

        System.out.println("查询到的用户: " + sysUser.getUserName());
        System.out.println("用户邮箱: " + sysUser.getEmail());
        System.out.println("用户手机: " + sysUser.getPhonenumber());

        // 创建一个新的Map，只包含用户需要的信息
        Map<String, Object> profile = new HashMap<>();
        profile.put("userId", sysUser.getUserId());
        profile.put("userName", sysUser.getUserName());
        profile.put("nickName", sysUser.getNickName());
        profile.put("email", sysUser.getEmail());
        profile.put("phonenumber", sysUser.getPhonenumber());
        profile.put("sex", sysUser.getSex());
        profile.put("avatar", sysUser.getAvatar());
        profile.put("status", sysUser.getStatus());
        profile.put("balance", sysUser.getBalance());
        profile.put("userType", sysUser.getUserType());

        System.out.println("=== 成功获取用户信息 ===");
        return success(profile);
    }

    /**
     * 新增用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user)
    {
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user)))
        {
            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user)))
        {
            return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user)))
        {
            return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setCreateBy(getUsername());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user)))
        {
            return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber())
                && UserConstants.NOT_UNIQUE.equals(userService.checkPhoneUnique(user)))
        {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail())
                && UserConstants.NOT_UNIQUE.equals(userService.checkEmailUnique(user)))
        {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable Long[] userIds)
    {
        if (ArrayUtils.contains(userIds, getUserId()))
        {
            return error("当前用户不能删除");
        }
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @PreAuthorize("@ss.hasPermi('system:user:resetPwd')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setUpdateBy(getUsername());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setUpdateBy(getUsername());
        return toAjax(userService.updateUserStatus(user));
    }

    /**
     * 根据用户编号获取授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId)
    {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        return ajax;
    }

    /**
     * 用户授权角色
     */
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户管理", businessType = BusinessType.GRANT)
    @PutMapping("/authRole")
    public AjaxResult insertAuthRole(Long userId, Long[] roleIds)
    {
        userService.checkUserDataScope(userId);
        userService.insertUserAuth(userId, roleIds);
        return success();
    }

    /**
     * 获取部门树列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/deptTree")
    public AjaxResult deptTree(SysDept dept)
    {
        return success(deptService.selectDeptTreeList(dept));
    }
}
