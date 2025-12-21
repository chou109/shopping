package com.neuedu.system.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.neuedu.common.annotation.Log;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.system.domain.UserFavorite;
import com.neuedu.system.service.IUserFavoriteService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 用户收藏Controller
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@RestController
@RequestMapping("/system/favorite")
@Tag(name = "【用户收藏】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserFavoriteController extends BaseController
{
    private final IUserFavoriteService userFavoriteService;

    /**
     * 查询用户收藏列表
     */
    @Operation(summary = "查询用户收藏列表")
    @PreAuthorize("@ss.hasPermi('system:favorite:list')")
    @GetMapping("/list")
    public TableDataInfo list(UserFavorite userFavorite)
    {
        startPage();
        List<UserFavorite> list = userFavoriteService.selectUserFavoriteList(userFavorite);
        return getDataTable(list);
    }

    /**
     * 导出用户收藏列表
     */
    @Operation(summary = "导出用户收藏列表")
    @PreAuthorize("@ss.hasPermi('system:favorite:export')")
    @Log(title = "用户收藏", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, UserFavorite userFavorite)
    {
        List<UserFavorite> list = userFavoriteService.selectUserFavoriteList(userFavorite);
        ExcelUtil<UserFavorite> util = new ExcelUtil<UserFavorite>(UserFavorite.class);
        util.exportExcel(response, list, "用户收藏数据");
    }

    /**
     * 获取用户收藏详细信息
     */
    @Operation(summary = "获取用户收藏详细信息")
    @PreAuthorize("@ss.hasPermi('system:favorite:query')")
    @GetMapping(value = "/{favoriteId}")
    public AjaxResult getInfo(@PathVariable("favoriteId") Long favoriteId)
    {
        return success(userFavoriteService.selectUserFavoriteByFavoriteId(favoriteId));
    }

    /**
     * 新增用户收藏
     */
    @Operation(summary = "新增用户收藏")
    @PreAuthorize("@ss.hasPermi('system:favorite:add')")
    @Log(title = "用户收藏", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody UserFavorite userFavorite)
    {
        return toAjax(userFavoriteService.insertUserFavorite(userFavorite));
    }

    /**
     * 修改用户收藏
     */
    @Operation(summary = "修改用户收藏")
    @PreAuthorize("@ss.hasPermi('system:favorite:edit')")
    @Log(title = "用户收藏", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody UserFavorite userFavorite)
    {
        return toAjax(userFavoriteService.updateUserFavorite(userFavorite));
    }

    /**
     * 删除用户收藏
     */
    @Operation(summary = "删除用户收藏")
    @PreAuthorize("@ss.hasPermi('system:favorite:remove')")
    @Log(title = "用户收藏", businessType = BusinessType.DELETE)
	@DeleteMapping("/{favoriteIds}")
    public AjaxResult remove(@PathVariable( name = "favoriteIds" ) Long[] favoriteIds) 
    {
        return toAjax(userFavoriteService.deleteUserFavoriteByFavoriteIds(favoriteIds));
    }
}
