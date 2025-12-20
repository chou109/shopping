package com.neuedu.system.controller;

import java.util.List;

import com.neuedu.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.neuedu.common.annotation.Log;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.system.domain.UserAddress;
import com.neuedu.system.service.IUserAddressService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 用户地址Controller
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@RestController
@RequestMapping("/system/address")
@Tag(name = "【用户地址】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserAddressController extends BaseController
{
    private final IUserAddressService userAddressService;

    /**
     * 查询用户地址列表
     */
    @Operation(summary = "查询用户地址列表")
    @PreAuthorize("@ss.hasPermi('system:address:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserAddress userAddress)
    {
        startPage();
        List<UserAddress> list = userAddressService.selectUserAddressList(userAddress);
        return getDataTable(list);
    }

    /**
     * 根据用户ID查询地址列表
     */
    @Operation(summary = "根据用户ID查询地址列表")
    @GetMapping("/user/{userId}")
    public AjaxResult listByUserId(@PathVariable Long userId) {
        List<UserAddress> list = userAddressService.selectUserAddressListByUserId(userId);
        return success(list);
    }

    /**
     * 获取用户的默认地址
     */
    @Operation(summary = "获取用户的默认地址")
    @GetMapping("/user/{userId}/default")
    public AjaxResult getDefaultAddress(@PathVariable Long userId) {
        UserAddress address = userAddressService.selectDefaultAddressByUserId(userId);
        return success(address);
    }

    /**
     * 导出用户地址列表
     */
    @Operation(summary = "导出用户地址列表")
    @PreAuthorize("@ss.hasPermi('system:address:export')")
    @Log(title = "用户地址", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserAddress userAddress)
    {
        List<UserAddress> list = userAddressService.selectUserAddressList(userAddress);
        ExcelUtil<UserAddress> util = new ExcelUtil<UserAddress>(UserAddress.class);
        util.exportExcel(response, list, "用户地址数据");
    }

    /**
     * 获取用户地址详细信息
     */
    @Operation(summary = "获取用户地址详细信息")
    @PreAuthorize("@ss.hasPermi('system:address:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(userAddressService.selectUserAddressById(id));
    }

    /**
     * 新增用户地址
     */
    @Operation(summary = "新增用户地址")
    @PreAuthorize("@ss.hasPermi('system:address:add')")
    @Log(title = "用户地址", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody UserAddress userAddress)
    {
        // 验证地址信息
        if (!userAddressService.validateAddress(userAddress)) {
            return error("地址信息不完整，请填写所有必填项");
        }
        // 设置当前用户ID（如果未设置）
        if (userAddress.getUserId() == null) {
            // 这里应该从当前登录用户获取，根据你的认证系统调整
             userAddress.setUserId(getUserId());
// 获取当前登录用户ID
// Long userId = SecurityUtils.getUserId();
        }
        return toAjax(userAddressService.insertUserAddress(userAddress));
    }

    /**
     * 修改用户地址
     */
    @Operation(summary = "修改用户地址")
    @PreAuthorize("@ss.hasPermi('system:address:edit')")
    @Log(title = "用户地址", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody UserAddress userAddress)
    {
        // 验证地址信息
        if (!userAddressService.validateAddress(userAddress)) {
            return error("地址信息不完整，请填写所有必填项");
        }
        return toAjax(userAddressService.updateUserAddress(userAddress));
    }

    /**
     * 删除用户地址
     */
    @Operation(summary = "删除用户地址")
    @PreAuthorize("@ss.hasPermi('system:address:remove')")
    @Log(title = "用户地址", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        return toAjax(userAddressService.deleteUserAddressByIds(ids));
    }

    /**
     * 设置默认地址
     */
    @Operation(summary = "设置默认地址")
    @PostMapping("/setDefault")
    public AjaxResult setDefault(@RequestParam Long addressId,
                                 @RequestParam Long userId) {
        return toAjax(userAddressService.setDefaultAddress(addressId, userId));
    }

    /**
     * 验证地址信息
     */
    @Operation(summary = "验证地址信息")
    @PostMapping("/validate")
    public AjaxResult validate(@RequestBody UserAddress userAddress) {
        boolean isValid = userAddressService.validateAddress(userAddress);
        if (isValid) {
            return success("地址信息有效");
        } else {
            return error("地址信息不完整或格式不正确");
        }
    }



}
