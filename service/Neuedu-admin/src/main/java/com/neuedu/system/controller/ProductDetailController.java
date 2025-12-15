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
import com.neuedu.system.domain.ProductDetail;
import com.neuedu.system.service.IProductDetailService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 商品明细Controller
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@RestController
@RequestMapping("/system/productDetail")
@Tag(name = "【商品明细】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductDetailController extends BaseController
{
    private final IProductDetailService productDetailService;

    /**
     * 查询商品明细列表
     */
    @Operation(summary = "查询商品明细列表")
    @PreAuthorize("@ss.hasPermi('system:detail:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProductDetail productDetail)
    {
        startPage();
        List<ProductDetail> list = productDetailService.selectProductDetailList(productDetail);
        return getDataTable(list);
    }

    /**
     * 导出商品明细列表
     */
    @Operation(summary = "导出商品明细列表")
    @PreAuthorize("@ss.hasPermi('system:detail:export')")
    @Log(title = "商品明细", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductDetail productDetail)
    {
        List<ProductDetail> list = productDetailService.selectProductDetailList(productDetail);
        ExcelUtil<ProductDetail> util = new ExcelUtil<ProductDetail>(ProductDetail.class);
        util.exportExcel(response, list, "商品明细数据");
    }

    /**
     * 获取商品明细详细信息
     */
    @Operation(summary = "获取商品明细详细信息")
    @PreAuthorize("@ss.hasPermi('system:detail:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(productDetailService.selectProductDetailById(id));
    }

    /**
     * 新增商品明细
     */
    @Operation(summary = "新增商品明细")
    @PreAuthorize("@ss.hasPermi('system:detail:add')")
    @Log(title = "商品明细", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductDetail productDetail)
    {
        return toAjax(productDetailService.insertProductDetail(productDetail));
    }

    /**
     * 修改商品明细
     */
    @Operation(summary = "修改商品明细")
    @PreAuthorize("@ss.hasPermi('system:detail:edit')")
    @Log(title = "商品明细", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProductDetail productDetail)
    {
        return toAjax(productDetailService.updateProductDetail(productDetail));
    }

    /**
     * 删除商品明细
     */
    @Operation(summary = "删除商品明细")
    @PreAuthorize("@ss.hasPermi('system:detail:remove')")
    @Log(title = "商品明细", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        return toAjax(productDetailService.deleteProductDetailByIds(ids));
    }
}
