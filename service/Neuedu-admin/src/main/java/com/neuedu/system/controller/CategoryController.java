package com.neuedu.system.controller;

import java.util.List;

import com.neuedu.system.domain.CategoryTreeNode;
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
import com.neuedu.system.domain.Category;
import com.neuedu.system.service.ICategoryService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 类目Controller
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@RestController
@RequestMapping("/system/category")
@Tag(name = "【类目】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CategoryController extends BaseController
{
    private final ICategoryService categoryService;

    /**
     * 查询类目列表
     */
    @Operation(summary = "查询类目列表")
    @PreAuthorize("@ss.hasPermi('system:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(Category category)
    {
        startPage();
        List<Category> list = categoryService.selectCategoryList(category);
        return getDataTable(list);
    }

    /**
     * 获取分类树形结构
     */
    @Operation(summary = "获取分类树形结构")
    @GetMapping("/tree")
    public AjaxResult tree() {
        List<CategoryTreeNode> tree = categoryService.buildCategoryTree();
        return success(tree);
    }

    /**
     * 获取父分类下拉树列表
     */
    @Operation(summary = "获取父分类下拉树列表")
    @GetMapping("/treeselect")
    public AjaxResult treeselect() {
        List<CategoryTreeNode> tree = categoryService.buildCategoryTree();
        return success(tree);
    }

    /**
     * 导出类目列表
     */
    @Operation(summary = "导出类目列表")
    @PreAuthorize("@ss.hasPermi('system:category:export')")
    @Log(title = "类目", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Category category)
    {
        List<Category> list = categoryService.selectCategoryList(category);
        ExcelUtil<Category> util = new ExcelUtil<Category>(Category.class);
        util.exportExcel(response, list, "类目数据");
    }

    /**
     * 获取类目详细信息
     */
    @Operation(summary = "获取类目详细信息")
    @PreAuthorize("@ss.hasPermi('system:category:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(categoryService.selectCategoryById(id));
    }

    /**
     * 新增类目
     */
    @Operation(summary = "新增类目")
    @PreAuthorize("@ss.hasPermi('system:category:add')")
    @Log(title = "类目", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Category category)
    {
        // 验证分类名称是否唯一
        if (!categoryService.checkCategoryNameUnique(category)) {
            return error("新增分类'" + category.getName() + "'失败，分类名称已存在");
        }
        return toAjax(categoryService.insertCategory(category));
    }

    /**
     * 修改类目
     */
    @Operation(summary = "修改类目")
    @PreAuthorize("@ss.hasPermi('system:category:edit')")
    @Log(title = "类目", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Category category)
    {

        // 验证分类名称是否唯一
        if (!categoryService.checkCategoryNameUnique(category)) {
            return error("修改分类'" + category.getName() + "'失败，分类名称已存在");
        }
        // 检查是否设置了自身为父分类
        if (category.getId().equals(category.getParentId())) {
            return error("不能将自身设置为父分类");
        }
        return toAjax(categoryService.updateCategory(category));
    }

    /**
     * 删除类目
     */
    @Operation(summary = "删除类目")
    @PreAuthorize("@ss.hasPermi('system:category:remove')")
    @Log(title = "类目", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        return toAjax(categoryService.deleteCategoryByIds(ids));
    }

    /**
     * 启用/禁用分类
     */
    @Operation(summary = "启用/禁用分类")
    @PreAuthorize("@ss.hasPermi('system:category:edit')")
    @Log(title = "分类状态", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody Category category) {
        return toAjax(categoryService.changeStatus(category.getId(), category.getStatus()));
    }

    /**
     * 设置/取消热门分类
     */
    @Operation(summary = "设置/取消热门分类")
    @PreAuthorize("@ss.hasPermi('system:category:edit')")
    @Log(title = "热门分类", businessType = BusinessType.UPDATE)
    @PutMapping("/changeHotFlag")
    public AjaxResult changeHotFlag(@RequestBody Category category) {
        return toAjax(categoryService.changeHotFlag(category.getId(), category.getHotFlag()));
    }

    // ================ 前台接口（无需权限） ================

    /**
     * 获取启用状态的分类列表（前台接口）
     */
    @Operation(summary = "获取启用状态的分类列表")
    @GetMapping("/enabled/list")
    public AjaxResult getEnabledCategories() {
        List<Category> list = categoryService.selectEnabledCategoryList();
        return success(list);
    }

    /**
     * 获取热门分类列表（前台接口）
     */
    @Operation(summary = "获取热门分类列表")
    @GetMapping("/hot/list")
    public AjaxResult getHotCategories() {
        List<Category> list = categoryService.selectHotCategoryList();
        return success(list);
    }

    /**
     * 获取分类树（前台接口）
     */
    @Operation(summary = "获取分类树")
    @GetMapping("/front/tree")
    public AjaxResult getFrontCategoryTree() {
        List<CategoryTreeNode> tree = categoryService.buildCategoryTree();
        return success(tree);
    }
}
