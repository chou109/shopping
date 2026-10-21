package com.neuedu.system.controller;

import java.util.List;
import java.util.Map;

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
     * 查询用户地址列表（管理员用）
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
     * 查询当前登录用户的所有地址
     */
    @Operation(summary = "查询当前用户地址列表")
    @GetMapping("/my")
    public AjaxResult getMyAddressList() {
        Long userId = getUserId();
        List<UserAddress> list = userAddressService.selectUserAddressListByUserId(userId);
        return success(list);
    }

    /**
     * 根据用户ID查询地址列表（管理员用）
     */
    @Operation(summary = "根据用户ID查询地址列表")
    @PreAuthorize("@ss.hasPermi('system:address:query')")
    @GetMapping("/user/{userId}")
    public AjaxResult listByUserId(@PathVariable Long userId) {
        List<UserAddress> list = userAddressService.selectUserAddressListByUserId(userId);
        return success(list);
    }

    /**
     * 获取当前登录用户的默认地址
     */
    @Operation(summary = "获取当前用户的默认地址")
    @GetMapping("/my/default")
    public AjaxResult getMyDefaultAddress() {
        Long userId = getUserId();
        UserAddress address = userAddressService.selectDefaultAddressByUserId(userId);
        return success(address);
    }

    /**
     * 获取用户的默认地址（管理员用）
     */
    @Operation(summary = "获取用户的默认地址")
    @PreAuthorize("@ss.hasPermi('system:address:query')")
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
        UserAddress address = userAddressService.selectUserAddressById(id);

        // 权限校验：普通用户只能查看自己的地址，管理员可以查看所有
        Long currentUserId = getUserId();
        if (!SecurityUtils.isAdmin(currentUserId) && address != null) {
            if (!address.getCreateUser().equals(currentUserId)) {
                return error("没有权限查看此地址");
            }
        }

        return success(address);
    }

    /**
     * 新增用户地址（当前用户）
     */
    @Operation(summary = "新增用户地址")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody UserAddress userAddress)
    {
        // 验证地址信息
        if (!userAddressService.validateAddress(userAddress)) {
            return error("地址信息不完整，请填写所有必填项");
        }

        // 获取当前登录用户ID
        Long currentUserId = getUserId();

        // 设置用户ID到createUser字段
        userAddress.setCreateUser(currentUserId);

        // 设置更新人
        userAddress.setUpdateUser(currentUserId);

        return toAjax(userAddressService.insertUserAddress(userAddress));
    }

    /**
     * 管理员新增用户地址
     */
    @Operation(summary = "管理员新增用户地址")
    @PreAuthorize("@ss.hasPermi('system:address:add')")
    @Log(title = "用户地址", businessType = BusinessType.INSERT)
    @PostMapping("/admin")
    public AjaxResult addByAdmin(@Validated @RequestBody UserAddress userAddress)
    {
        // 验证地址信息
        if (!userAddressService.validateAddress(userAddress)) {
            return error("地址信息不完整，请填写所有必填项");
        }

        // 如果还没有设置创建人，使用当前用户
        if (userAddress.getCreateUser() == null) {
            Long currentUserId = getUserId();
            userAddress.setCreateUser(currentUserId);
        }

        return toAjax(userAddressService.insertUserAddress(userAddress));
    }

    /**
     * 修改用户地址
     */
    @Operation(summary = "修改用户地址")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody UserAddress userAddress)
    {
        // 验证地址信息
        if (!userAddressService.validateAddress(userAddress)) {
            return error("地址信息不完整，请填写所有必填项");
        }

        // 权限校验：普通用户只能修改自己的地址，管理员可以修改所有
        UserAddress existingAddress = userAddressService.selectUserAddressById(userAddress.getId());
        if (existingAddress == null) {
            return error("地址不存在");
        }

        Long currentUserId = getUserId();

        if (!SecurityUtils.isAdmin(currentUserId) && !existingAddress.getCreateUser().equals(currentUserId)) {
            return error("没有权限修改此地址");
        }

        // 设置更新人
        userAddress.setUpdateUser(currentUserId);

        // 保持原有创建人
        userAddress.setCreateUser(existingAddress.getCreateUser());

        int result = userAddressService.updateUserAddress(userAddress);

        return toAjax(result);
    }

    /**
     * 管理员修改用户地址
     */
    @Operation(summary = "管理员修改用户地址")
    @PreAuthorize("@ss.hasPermi('system:address:edit')")
    @Log(title = "用户地址", businessType = BusinessType.UPDATE)
    @PutMapping("/admin")
    public AjaxResult editByAdmin(@Validated @RequestBody UserAddress userAddress)
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
    @DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        // 权限校验：普通用户只能删除自己的地址，管理员可以删除所有
        UserAddress address = userAddressService.selectUserAddressById(id);
        if (address == null) {
            return error("地址不存在");
        }

        Long currentUserId = getUserId();
        if (!SecurityUtils.isAdmin(currentUserId) && !address.getCreateUser().equals(currentUserId)) {
            return error("没有权限删除此地址");
        }

        int result = userAddressService.deleteUserAddressById(id);
        return toAjax(result);
    }

    /**
     * 批量删除用户地址（管理员用）
     */
    @Operation(summary = "批量删除用户地址")
    @PreAuthorize("@ss.hasPermi('system:address:remove')")
    @Log(title = "用户地址", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch/{ids}")
    public AjaxResult removeBatch(@PathVariable("ids") Long[] ids)
    {
        return toAjax(userAddressService.deleteUserAddressByIds(ids));
    }

    /**
     * 设置默认地址（当前用户）
     * 注意：前端传递的是 {addressId: xxx}，不需要userId参数
     */
    @Operation(summary = "设置默认地址")
    @PostMapping("/setDefault")
    public AjaxResult setDefault(@RequestBody Map<String, Long> params)
    {
        Long addressId = params.get("addressId");
        if (addressId == null) {
            return error("地址ID不能为空");
        }

        Long userId = getUserId();
        return toAjax(userAddressService.setDefaultAddress(addressId, userId));
    }

    /**
     * 验证地址信息
     * 注意：前端需要传递完整的地址信息进行验证
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

    /**
     * 获取当前用户的地址数量
     */
    @Operation(summary = "获取当前用户的地址数量")
    @GetMapping("/my/count")
    public AjaxResult countMyAddress() {
        Long userId = getUserId();
        int count = userAddressService.countUserAddress(userId);
        return success(count);
    }

    /**
     * 获取用户的地址数量（管理员用）
     */
    @Operation(summary = "获取用户的地址数量")
    @PreAuthorize("@ss.hasPermi('system:address:query')")
    @GetMapping("/user/{userId}/count")
    public AjaxResult countUserAddress(@PathVariable Long userId) {
        int count = userAddressService.countUserAddress(userId);
        return success(count);
    }

    /**
     * 获取当前登录用户ID
     */
    public Long getUserId() {
        try {
            return SecurityUtils.getUserId();
        } catch (Exception e) {
            throw new RuntimeException("用户未登录或登录信息失效");
        }
    }
}
