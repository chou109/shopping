package com.neuedu.system.controller;

import java.util.List;

import com.neuedu.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.neuedu.common.annotation.Log;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.system.domain.UserFootprint;
import com.neuedu.system.service.IUserFootprintService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 用户足迹Controller
 * 
 * @author ruoyi
 * @date 2025-12-22
 */
@RestController
@RequestMapping("/system/footprint")
@Tag(name = "【用户足迹】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserFootprintController extends BaseController
{
    private final IUserFootprintService userFootprintService;

    /**
     * 查询用户足迹列表
     */
    @Operation(summary = "查询用户足迹列表")
    @PreAuthorize("@ss.hasPermi('system:footprint:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserFootprint userFootprint)
    {
        startPage();
        List<UserFootprint> list = userFootprintService.selectUserFootprintList(userFootprint);
        return getDataTable(list);
    }

    /**
     * 导出用户足迹列表
     */
    @Operation(summary = "导出用户足迹列表")
    @PreAuthorize("@ss.hasPermi('system:footprint:export')")
    @Log(title = "用户足迹", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserFootprint userFootprint)
    {
        List<UserFootprint> list = userFootprintService.selectUserFootprintList(userFootprint);
        ExcelUtil<UserFootprint> util = new ExcelUtil<UserFootprint>(UserFootprint.class);
        util.exportExcel(response, list, "用户足迹数据");
    }



    /**
     * 获取用户足迹详细信息
     */
    @Operation(summary = "获取用户足迹详细信息")
    @PreAuthorize("@ss.hasPermi('system:footprint:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(userFootprintService.selectUserFootprintById(id));
    }

    /**
     * 新增用户足迹
     */
    @Operation(summary = "新增用户足迹")
    @PreAuthorize("@ss.hasPermi('system:footprint:add')")
    @Log(title = "用户足迹", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserFootprint userFootprint)
    {
        return toAjax(userFootprintService.insertUserFootprint(userFootprint));
    }

    /**
     * 修改用户足迹
     */
    @Operation(summary = "修改用户足迹")
    @PreAuthorize("@ss.hasPermi('system:footprint:edit')")
    @Log(title = "用户足迹", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserFootprint userFootprint)
    {
        return toAjax(userFootprintService.updateUserFootprint(userFootprint));
    }

    /**
     * 删除用户足迹
     */
    @Operation(summary = "删除用户足迹")
    @PreAuthorize("@ss.hasPermi('system:footprint:remove')")
    @Log(title = "用户足迹", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        return toAjax(userFootprintService.deleteUserFootprintByIds(ids));
    }

    // ============ 新增的API接口 ============

    /**
     * 记录用户足迹（供前端调用）
     */
    @Operation(summary = "记录用户足迹")
    @PostMapping("/record")
    public AjaxResult record(@RequestParam Integer targetType,
                             @RequestParam Long targetId)
    {
        // 设置当前用户ID
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }
        return toAjax(userFootprintService.recordFootprint(userId, targetType, targetId));
    }



    /**
     * 查询用户的足迹列表
     */
    @Operation(summary = "查询用户的足迹列表")
    @PreAuthorize("@ss.hasPermi('system:footprint:list')")
    @GetMapping("/user/{userId}")
    public TableDataInfo listByUser(@PathVariable Long userId,
                                    @RequestParam(value = "targetType", required = false) Integer targetType)
    {
        startPage();
        List<UserFootprint> list = userFootprintService.selectFootprintByUserId(userId, targetType);
        return getDataTable(list);
    }

    /**
     * 查询用户最近浏览
     */
    @Operation(summary = "查询用户最近浏览")
    @GetMapping("/recent/{userId}")
    public AjaxResult getRecent(@PathVariable Long userId,
                                @RequestParam(value = "limit", defaultValue = "10") Integer limit)
    {
        List<UserFootprint> list = userFootprintService.selectRecentFootprint(userId, limit);
        return success(list);
    }

    /**
     * 清空用户足迹
     */
    @Operation(summary = "清空用户足迹")
    @PreAuthorize("@ss.hasPermi('system:footprint:remove')")
    @Log(title = "用户足迹", businessType = BusinessType.DELETE)
    @DeleteMapping("/clear/{userId}")
    public AjaxResult clear(@PathVariable Long userId)
    {
        return toAjax(userFootprintService.clearUserFootprint(userId));
    }

    /**
     * 统计用户足迹数量
     */
    @Operation(summary = "统计用户足迹数量")
    @GetMapping("/count/{userId}")
    public AjaxResult count(@PathVariable Long userId)
    {
        int count = userFootprintService.countUserFootprint(userId);
        return success(count);
    }
}
