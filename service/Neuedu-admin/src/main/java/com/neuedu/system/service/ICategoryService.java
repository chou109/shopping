package com.neuedu.system.service;

import java.util.List;
import com.neuedu.system.domain.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import com.neuedu.system.domain.CategoryTreeNode;

/**
 * 类目Service接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
public interface ICategoryService extends IService<Category>
{
    /**
     * 查询类目
     * 
     * @param id 类目主键
     * @return 类目
     */
    public Category selectCategoryById(Long id);

    /**
     * 查询类目列表
     * 
     * @param category 类目
     * @return 类目集合
     */
    public List<Category> selectCategoryList(Category category);

    /**
     * 查询启用状态的分类列表（用于前台展示）
     */
    public List<Category> selectEnabledCategoryList();

    /**
     * 新增类目
     * 
     * @param category 类目
     * @return 结果
     */
    public int insertCategory(Category category);

    /**
     * 修改类目
     * 
     * @param category 类目
     * @return 结果
     */
    public int updateCategory(Category category);

    /**
     * 批量删除类目
     * 
     * @param ids 需要删除的类目主键集合
     * @return 结果
     */
    public int deleteCategoryByIds(Long[] ids);

    /**
     * 删除类目信息
     * 
     * @param id 类目主键
     * @return 结果
     */
    public int deleteCategoryById(Long id);


    // ================ 新增功能 ================

    /**
     * 构建分类树形结构
     */
    public List<CategoryTreeNode> buildCategoryTree();

    /**
     * 获取热门分类列表
     */
    public List<Category> selectHotCategoryList();

    /**
     * 获取分类的所有子分类ID（包括自身）
     */
    public List<Long> getAllSubCategoryIds(Long categoryId);

    /**
     * 检查分类名称是否唯一
     */
    public boolean checkCategoryNameUnique(Category category);

    /**
     * 启用/禁用分类
     */
    public int changeStatus(Long id, Integer status);

    /**
     * 设置/取消热门分类
     */
    public int changeHotFlag(Long id, Integer hotFlag);

}
