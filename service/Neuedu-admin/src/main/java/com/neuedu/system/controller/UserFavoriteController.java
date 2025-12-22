package com.neuedu.system.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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


    // ============ 新增的接口 ============

    /**
     * 添加收藏
     */
    @Operation(summary = "添加收藏")
    @PostMapping("/add")
    public AjaxResult addFavorite(@RequestBody Map<String, Object> params) {
        Integer targetType = Integer.valueOf(params.get("targetType").toString());
        Long targetId = Long.valueOf(params.get("targetId").toString());

        boolean result = userFavoriteService.addFavorite(targetType, targetId);
        return success(result);
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏")
    @PostMapping("/remove")
    public AjaxResult removeFavorite(@RequestBody Map<String, Object> params) {
        Integer targetType = Integer.valueOf(params.get("targetType").toString());
        Long targetId = Long.valueOf(params.get("targetId").toString());

        boolean result = userFavoriteService.removeFavorite(targetType, targetId);
        return success(result);
    }

    /**
     * 检查是否已收藏
     */
    @Operation(summary = "检查是否已收藏")
    @GetMapping("/check")
    public AjaxResult checkFavorite(@RequestParam Integer targetType,
                                    @RequestParam Long targetId) {
        boolean isFavorite = userFavoriteService.isFavorite(targetType, targetId);
        Map<String, Object> result = new HashMap<>();
        result.put("isFavorite", isFavorite);
        return success(result);
    }

    /**
     * 我的商品收藏列表
     */
    @Operation(summary = "我的商品收藏列表")
    @GetMapping("/my/products")
    public AjaxResult myProductFavorites() {
        List<UserFavorite> list = userFavoriteService.getUserProductFavorites();
        return success(list);
    }

    /**
     * 我的店铺收藏列表
     */
    @Operation(summary = "我的店铺收藏列表")
    @GetMapping("/my/shops")
    public AjaxResult myShopFavorites() {
        List<UserFavorite> list = userFavoriteService.getUserShopFavorites();
        return success(list);
    }

    /**
     * 我的收藏统计
     */
    @Operation(summary = "我的收藏统计")
    @GetMapping("/my/stats")
    public AjaxResult myFavoriteStats() {
        Map<String, Object> stats = new HashMap<>();

        Long productCount = userFavoriteService.getUserFavoriteCount(1);
        Long shopCount = userFavoriteService.getUserFavoriteCount(2);
        Long totalCount = userFavoriteService.getUserFavoriteCount(null);

        stats.put("productCount", productCount);
        stats.put("shopCount", shopCount);
        stats.put("totalCount", totalCount);

        return success(stats);
    }

    /**
     * 获取收藏数量
     */
    @Operation(summary = "获取收藏数量")
    @GetMapping("/count")
    public AjaxResult getFavoriteCount(@RequestParam Integer targetType,
                                       @RequestParam Long targetId) {
        Long count = userFavoriteService.getTargetFavoriteCount(targetType, targetId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        return success(result);
    }

    /**
     * 切换收藏状态
     */
    @Operation(summary = "切换收藏状态")
    @PostMapping("/toggle")
    public AjaxResult toggleFavorite(@RequestBody Map<String, Object> params) {
        Integer targetType = Integer.valueOf(params.get("targetType").toString());
        Long targetId = Long.valueOf(params.get("targetId").toString());

        boolean isFavorite = userFavoriteService.isFavorite(targetType, targetId);

        if (isFavorite) {
            // 如果已收藏，取消收藏
            userFavoriteService.removeFavorite(targetType, targetId);
            return success(false); // 返回false表示取消收藏
        } else {
            // 如果未收藏，添加收藏
            userFavoriteService.addFavorite(targetType, targetId);
            return success(true); // 返回true表示已收藏
        }
    }
}
