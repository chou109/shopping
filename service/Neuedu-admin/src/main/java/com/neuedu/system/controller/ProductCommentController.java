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
import com.neuedu.system.domain.ProductComment;
import com.neuedu.system.service.IProductCommentService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 商品评论Controller
 * 
 * @author ruoyi
 * @date 2025-12-21
 */
@RestController
@RequestMapping("/system/comment")
@Tag(name = "【商品评论】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductCommentController extends BaseController
{
    private final IProductCommentService productCommentService;

    /**
     * 查询商品评论列表
     */
    @Operation(summary = "查询商品评论列表")
    @PreAuthorize("@ss.hasPermi('system:comment:list')")
    @GetMapping("/list")
    public TableDataInfo list(ProductComment productComment)
    {
        startPage();
        List<ProductComment> list = productCommentService.selectProductCommentList(productComment);
        return getDataTable(list);
    }

    /**
     * 导出商品评论列表
     */
    @Operation(summary = "导出商品评论列表")
    @PreAuthorize("@ss.hasPermi('system:comment:export')")
    @Log(title = "商品评论", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ProductComment productComment)
    {
        List<ProductComment> list = productCommentService.selectProductCommentList(productComment);
        ExcelUtil<ProductComment> util = new ExcelUtil<ProductComment>(ProductComment.class);
        util.exportExcel(response, list, "商品评论数据");
    }

    /**
     * 获取商品评论详细信息
     */
    @Operation(summary = "获取商品评论详细信息")
    @PreAuthorize("@ss.hasPermi('system:comment:query')")
    @GetMapping(value = "/{commentId}")
    public AjaxResult getInfo(@PathVariable("commentId") Long commentId)
    {
        return success(productCommentService.selectProductCommentByCommentId(commentId));
    }

    /**
     * 新增商品评论
     */
    @Operation(summary = "新增商品评论")
    @PreAuthorize("@ss.hasPermi('system:comment:add')")
    @Log(title = "商品评论", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ProductComment productComment)
    {
        return toAjax(productCommentService.insertProductComment(productComment));
    }

    /**
     * 修改商品评论
     */
    @Operation(summary = "修改商品评论")
    @PreAuthorize("@ss.hasPermi('system:comment:edit')")
    @Log(title = "商品评论", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ProductComment productComment)
    {
        return toAjax(productCommentService.updateProductComment(productComment));
    }

    /**
     * 删除商品评论
     */
    @Operation(summary = "删除商品评论")
    @PreAuthorize("@ss.hasPermi('system:comment:remove')")
    @Log(title = "商品评论", businessType = BusinessType.DELETE)
	@DeleteMapping("/{commentIds}")
    public AjaxResult remove(@PathVariable( name = "commentIds" ) Long[] commentIds) 
    {
        return toAjax(productCommentService.deleteProductCommentByCommentIds(commentIds));
    }
}
