package com.neuedu.system.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.neuedu.common.annotation.Log;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.system.domain.User;
import com.neuedu.system.service.IUserService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 用户信息Controller
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@RestController
@RequestMapping("/system/customer")
@Tag(name = "【用户信息】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserController extends BaseController
{
    private final IUserService userService;

    /**
     * 查询用户信息列表
     */
    @Operation(summary = "查询用户信息列表")
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(User user)
    {
        startPage();
        List<User> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    /**
     * 导出用户信息列表
     */
    @Operation(summary = "导出用户信息列表")
    @PreAuthorize("@ss.hasPermi('system:user:export')")
    @Log(title = "用户信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, User user)
    {
        List<User> list = userService.selectUserList(user);
        ExcelUtil<User> util = new ExcelUtil<User>(User.class);
        util.exportExcel(response, list, "用户信息数据");
    }

    /**
     * 获取用户信息详细信息
     */
    @Operation(summary = "获取用户信息详细信息")
    @PreAuthorize("@ss.hasPermi('system:user:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(userService.selectUserById(id));
    }

    /**
     * 新增用户信息
     */
    @Operation(summary = "新增用户信息")
    @PreAuthorize("@ss.hasPermi('system:user:add')")
    @Log(title = "用户信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody User user)
    {
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户信息
     */
    @Operation(summary = "修改用户信息")
    @PreAuthorize("@ss.hasPermi('system:user:edit')")
    @Log(title = "用户信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody User user)
    {
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户信息
     */
    @Operation(summary = "删除用户信息")
    @PreAuthorize("@ss.hasPermi('system:user:remove')")
    @Log(title = "用户信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        return toAjax(userService.deleteUserByIds(ids));
    }
    /**
     * 获取用户余额
     */
    @GetMapping("/{id}/balance")
    public AjaxResult getBalance(@PathVariable Long id)
    {
        return success(userService.getUserBalance(id));
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestParam String username, @RequestParam String password)
    {
        User user = userService.login(username, password);
        if (user != null) {
            // 这里可以生成token，设置session等
            return success("登录成功");
        }
        return error("登录失败");
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public AjaxResult register(@RequestBody User user)
    {
        boolean result = userService.register(user);
        if (result) {
            return success("注册成功");
        }
        return error("注册失败");
    }
}
