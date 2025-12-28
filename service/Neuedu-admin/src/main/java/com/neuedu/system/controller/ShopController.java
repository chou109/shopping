package com.neuedu.system.controller;

import java.math.BigDecimal;
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
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IShopService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 店铺信息Controller
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@RestController
@RequestMapping("/system/shop")
@Tag(name = "【店铺信息】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ShopController extends BaseController
{
    private final IShopService shopService;

    /**
     * 查询店铺信息列表
     */
    @Operation(summary = "查询店铺信息列表")
    @PreAuthorize("@ss.hasPermi('system:shop:list')")
    @GetMapping("/list")
    public TableDataInfo list(Shop shop)
    {
        startPage();
        List<Shop> list = shopService.selectShopList(shop);
        return getDataTable(list);
    }

    /**
     * 导出店铺信息列表
     */
    @Operation(summary = "导出店铺信息列表")
    @PreAuthorize("@ss.hasPermi('system:shop:export')")
    @Log(title = "店铺信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Shop shop)
    {
        List<Shop> list = shopService.selectShopList(shop);
        ExcelUtil<Shop> util = new ExcelUtil<Shop>(Shop.class);
        util.exportExcel(response, list, "店铺信息数据");
    }

    /**
     * 获取店铺信息详细信息
     */
    @Operation(summary = "获取店铺信息详细信息")
    @PreAuthorize("@ss.hasPermi('system:shop:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(shopService.selectShopById(id));
    }

    /**
     * 获取店铺公开信息（无需权限，用于前台展示）
     */
    @Operation(summary = "获取店铺公开信息")
    @GetMapping(value = "/public/{id}")
    public AjaxResult getPublicInfo(@PathVariable("id") Long id)
    {
        return success(shopService.selectShopById(id));
    }

    /**
     * 新增店铺信息
     */
    @Operation(summary = "新增店铺信息")
    @PreAuthorize("@ss.hasPermi('system:shop:add')")
    @Log(title = "店铺信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Shop shop)
    {
        return toAjax(shopService.insertShop(shop));
    }

    /**
     * 修改店铺信息
     */
    @Operation(summary = "修改店铺信息")
    @PreAuthorize("@ss.hasPermi('system:shop:edit')")
    @Log(title = "店铺信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Shop shop)
    {
        return toAjax(shopService.updateShop(shop));
    }

    /**
     * 删除店铺信息
     */
    @Operation(summary = "删除店铺信息")
    @PreAuthorize("@ss.hasPermi('system:shop:remove')")
    @Log(title = "店铺信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        return toAjax(shopService.deleteShopByIds(ids));
    }

    // ============ 新增的接口 ============

    /**
     * 获取我的店铺
     */
    @Operation(summary = "获取我的店铺")
    @PreAuthorize("@ss.hasPermi('system:shop:query')")
    @GetMapping("/my")
    public AjaxResult myShop() {
        return success(shopService.getMyShop());
    }

    /**
     * 更新店铺状态
     */
    @Operation(summary = "更新店铺状态")
    @PreAuthorize("@ss.hasPermi('system:shop:edit')")
    @Log(title = "店铺信息", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult updateStatus(@RequestBody Map<String, Object> params) {
        Long shopId = Long.valueOf(params.get("shopId").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        return toAjax(shopService.updateShopStatus(shopId, status));
    }

    /**
     * 逻辑删除店铺
     */
    @Operation(summary = "逻辑删除店铺")
    @PreAuthorize("@ss.hasPermi('system:shop:remove')")
    @Log(title = "店铺信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/logic/{id}")
    public AjaxResult removeLogic(@PathVariable Long id) {
        return toAjax(shopService.deleteShopLogicById(id));
    }

    /**
     * 批量逻辑删除店铺
     */
    @Operation(summary = "批量逻辑删除店铺")
    @PreAuthorize("@ss.hasPermi('system:shop:remove')")
    @Log(title = "店铺信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/logic/batch/{ids}")
    public AjaxResult removeLogicBatch(@PathVariable Long[] ids) {
        return toAjax(shopService.deleteShopLogicByIds(ids));
    }

    /**
     * 热门店铺列表
     */
    @Operation(summary = "热门店铺列表")
    @GetMapping("/hot")
    public AjaxResult hotList(@RequestParam(defaultValue = "10") Integer limit) {
        return success(shopService.getHotShopList(limit));
    }

    /**
     * 搜索店铺
     */
    @Operation(summary = "搜索店铺")
    @GetMapping("/search")
    public AjaxResult search(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer status) {
        return success(shopService.searchShops(keyword, status));
    }

    /**
     * 获取店铺统计信息
     */
    @Operation(summary = "获取店铺统计信息")
    @PreAuthorize("@ss.hasPermi('system:shop:query')")
    @GetMapping("/statistics/{id}")
    public AjaxResult getStatistics(@PathVariable Long id) {
        return success(shopService.getShopStatistics(id));
    }

    /**
     * 增加店铺销量（内部调用）
     */
    @Operation(summary = "增加店铺销量")
    @PreAuthorize("@ss.hasPermi('system:shop:edit')")
    @Log(title = "店铺信息", businessType = BusinessType.UPDATE)
    @PostMapping("/increaseSales/{shopId}")
    public AjaxResult increaseSales(@PathVariable Long shopId,
                                    @RequestParam Long quantity) {
        return toAjax(shopService.increaseSales(shopId, quantity));
    }

    /**
     * 更新店铺评分
     */
    @Operation(summary = "更新店铺评分")
    @PreAuthorize("@ss.hasPermi('system:shop:edit')")
    @Log(title = "店铺信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateScore/{shopId}")
    public AjaxResult updateScore(@PathVariable Long shopId,
                                  @RequestParam BigDecimal score) {
        return toAjax(shopService.updateShopScore(shopId, score));
    }

    /**
     * 更新店铺logo
     */
    @Operation(summary = "更新店铺logo")
    @PreAuthorize("@ss.hasPermi('system:shop:edit')")
    @Log(title = "店铺信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateLogo/{id}")
    public AjaxResult updateLogo(@PathVariable Long id,
                                 @RequestBody Map<String, String> params) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setShopLogo(params.get("logoUrl"));
        return toAjax(shopService.updateShop(shop));
    }

    /**
     * 更新店铺横幅
     */
    @Operation(summary = "更新店铺横幅")
    @PreAuthorize("@ss.hasPermi('system:shop:edit')")
    @Log(title = "店铺信息", businessType = BusinessType.UPDATE)
    @PostMapping("/updateBanner/{id}")
    public AjaxResult updateBanner(@PathVariable Long id,
                                   @RequestBody Map<String, String> params) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setShopBanner(params.get("bannerUrl"));
        return toAjax(shopService.updateShop(shop));
    }
}
