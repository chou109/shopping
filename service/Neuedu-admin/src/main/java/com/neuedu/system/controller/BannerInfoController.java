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
import com.neuedu.system.domain.BannerInfo;
import com.neuedu.system.service.IBannerInfoService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 【请填写功能名称】Controller
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@RestController
@RequestMapping("/system/bannerInfo")
@Tag(name = "【【请填写功能名称】】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BannerInfoController extends BaseController
{
    private final IBannerInfoService bannerInfoService;

    /**
     * 查询【请填写功能名称】列表
     */
    @Operation(summary = "查询【请填写功能名称】列表")
    @PreAuthorize("@ss.hasPermi('system:info:list')")
    @GetMapping("/list")
    public TableDataInfo list(BannerInfo bannerInfo)
    {
        startPage();
        List<BannerInfo> list = bannerInfoService.selectBannerInfoList(bannerInfo);
        return getDataTable(list);
    }

    /**
     * 导出【请填写功能名称】列表
     */
    @Operation(summary = "导出【请填写功能名称】列表")
    @PreAuthorize("@ss.hasPermi('system:info:export')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, BannerInfo bannerInfo)
    {
        List<BannerInfo> list = bannerInfoService.selectBannerInfoList(bannerInfo);
        ExcelUtil<BannerInfo> util = new ExcelUtil<BannerInfo>(BannerInfo.class);
        util.exportExcel(response, list, "【请填写功能名称】数据");
    }

    /**
     * 获取【请填写功能名称】详细信息
     */
    @Operation(summary = "获取【请填写功能名称】详细信息")
    @PreAuthorize("@ss.hasPermi('system:info:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(bannerInfoService.selectBannerInfoById(id));
    }

    /**
     * 新增【请填写功能名称】
     */
    @Operation(summary = "新增【请填写功能名称】")
    @PreAuthorize("@ss.hasPermi('system:info:add')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody BannerInfo bannerInfo)
    {
        return toAjax(bannerInfoService.insertBannerInfo(bannerInfo));
    }

    /**
     * 修改【请填写功能名称】
     */
    @Operation(summary = "修改【请填写功能名称】")
    @PreAuthorize("@ss.hasPermi('system:info:edit')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody BannerInfo bannerInfo)
    {
        return toAjax(bannerInfoService.updateBannerInfo(bannerInfo));
    }

    /**
     * 删除【请填写功能名称】
     */
    @Operation(summary = "删除【请填写功能名称】")
    @PreAuthorize("@ss.hasPermi('system:info:remove')")
    @Log(title = "【请填写功能名称】", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        return toAjax(bannerInfoService.deleteBannerInfoByIds(ids));
    }
}
