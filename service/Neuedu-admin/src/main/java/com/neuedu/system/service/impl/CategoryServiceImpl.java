package com.neuedu.system.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.neuedu.system.constants.CategoryConstants;
import com.neuedu.system.domain.CategoryTreeNode;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.CategoryMapper;
import com.neuedu.system.domain.Category;
import com.neuedu.system.service.ICategoryService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 类目Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService
{
    private final CategoryMapper categoryMapper;

    /**
     * 查询类目
     *
     * @param id 类目主键
     * @return 类目
     */
    @Override
    public Category selectCategoryById(Long id)
    {
        Category category = categoryMapper.selectCategoryById(id);
        // 设置父类目名称
        if (category != null && category.getParentId() != null && category.getParentId() > 0) {
            Category parent = categoryMapper.selectCategoryById(category.getParentId());
            if (parent != null) {
                category.setParentName(parent.getName());
            }
        }
        return category;
    }
    /**
     * 查询类目列表
     *
     * @param category 类目
     * @return 类目
     */
    @Override
    public List<Category> selectCategoryList(Category category)
    {
        List<Category> list = categoryMapper.selectCategoryList(category);
        // 为每个分类设置父类目名称
        Map<Long, String> parentNameMap = new HashMap<>();
        for (Category item : list) {
            if (item.getParentId() != null && item.getParentId() > 0) {
                String parentName = parentNameMap.get(item.getParentId());
                if (parentName == null) {
                    Category parent = getById(item.getParentId());
                    if (parent != null) {
                        parentName = parent.getName();
                        parentNameMap.put(item.getParentId(), parentName);
                    }
                }
                item.setParentName(parentName);
            }
        }
        return list;
    }
    /**
     * 新增类目
     * 
     * @param category 类目
     * @return 结果
     */
    @Override
    public int insertCategory(Category category)
    {
        // 使用常量设置默认值
        if (category.getStatus() == null) {
            category.setStatus(CategoryConstants.STATUS_ENABLED);  // 默认启用
        }
        if (category.getHotFlag() == null) {
            category.setStatus(CategoryConstants.HOT_FLAG_NO);  // 默认非热门
        }
        if (category.getSortOrder() == null) {
            // 获取当前最大排序号+1
            Long maxSortOrder = getMaxSortOrder();
            category.setSortOrder(maxSortOrder + 1);
        }
        category.setCreateTime(DateUtils.getNowDate());
        category.setDeleteFlag(CategoryConstants.DELETE_FLAG_NO);
        return categoryMapper.insertCategory(category);
    }

    /**
     * 查询启用状态的分类列表（用于前台展示）
     */
    @Override
    public List<Category> selectEnabledCategoryList() {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", CategoryConstants.STATUS_ENABLED)  // 启用状态
                .eq("delete_flag", CategoryConstants.DELETE_FLAG_NO)  // 未删除
                .orderByAsc("sort_order");  // 按排序号升序
        return list(queryWrapper);
    }

    /**
     * 修改类目
     * 
     * @param category 类目
     * @return 结果
     */
    @Override
    public int updateCategory(Category category)
    {
        category.setUpdateTime(DateUtils.getNowDate());
        return categoryMapper.updateCategory(category);
    }

    /**
     * 批量删除类目
     * 
     * @param ids 需要删除的类目主键
     * @return 结果
     */
    @Override
    public int deleteCategoryByIds(Long[] ids)
    {
        // 先检查是否有子分类，如果有则不允许删除
        for (Long id : ids) {
            if (hasChildren(id)) {
                throw new RuntimeException("分类ID[" + id + "]存在子分类，不能删除");
            }
        }
        return categoryMapper.deleteCategoryByIds(ids);
    }

    /**
     * 删除类目信息
     * 
     * @param id 类目主键
     * @return 结果
     */
    @Override
    public int deleteCategoryById(Long id)
    {
        // 检查是否有子分类
        if (hasChildren(id)) {
            throw new RuntimeException("该分类存在子分类，不能删除");
        }

        // 改为逻辑删除，设置删除标志
        Category category = new Category();
        category.setId(id);
        category.setDeleteFlag(CategoryConstants.DELETE_FLAG_YES);
        category.setUpdateTime(DateUtils.getNowDate());
        return updateCategory(category);
    }

    /**
     * 构建分类树形结构
     */
    @Override
    public List<CategoryTreeNode> buildCategoryTree() {
        // 传递常量值
        List<Category> allCategories = categoryMapper.selectCategoryTreeNodes(
                CategoryConstants.STATUS_ENABLED,
                CategoryConstants.DELETE_FLAG_NO
        );
        return buildTree(allCategories);
    }

    /**
     * 获取热门分类列表
     */
    @Override
    public List<Category> selectHotCategoryList() {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("hot_flag", CategoryConstants.HOT_FLAG_YES)  // 热门
                .eq("status", CategoryConstants.STATUS_ENABLED)     // 启用
                .eq("delete_flag", CategoryConstants.DELETE_FLAG_NO) // 未删除
                .orderByAsc("sort_order");
        return list(queryWrapper);
    }

    /**
     * 获取分类的所有子分类ID（包括自身）
     */
    @Override
    public List<Long> getAllSubCategoryIds(Long categoryId) {
        List<Long> result = new ArrayList<>();
        result.add(categoryId);
        getAllChildrenIds(categoryId, result);
        return result;
    }

    /**
     * 检查分类名称是否唯一
     */
    @Override
    public boolean checkCategoryNameUnique(Category category) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", category.getName())
                .eq("delete_flag", 0);
        if (category.getId() != null) {
            queryWrapper.ne("id", category.getId());
        }
        return count(queryWrapper) == 0;
    }

    /**
     * 启用/禁用分类
     */
    @Override
    @Transactional
    public int changeStatus(Long id, Integer status) {
        Category category = new Category();
        category.setId(id);
        category.setStatus(status);
        category.setUpdateTime(DateUtils.getNowDate());
        return updateCategory(category);
    }

    /**
     * 设置/取消热门分类
     */
    @Override
    @Transactional
    public int changeHotFlag(Long id, Integer hotFlag) {
        Category category = new Category();
        category.setId(id);
        category.setHotFlag(hotFlag);
        category.setUpdateTime(DateUtils.getNowDate());
        return updateCategory(category);
    }

    // ================ 私有方法 ================

    /**
     * 检查是否有子分类
     */
    private boolean hasChildren(Long parentId) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId)
                .eq("delete_flag", 0);
        return count(queryWrapper) > 0;
    }

    /**
     * 获取最大排序号
     */
    private Long getMaxSortOrder() {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("delete_flag", 0)
                .orderByDesc("sort_order")
                .last("limit 1");
        Category category = getOne(queryWrapper);
        return category != null && category.getSortOrder() != null ? category.getSortOrder() : 0L;
    }

    /**
     * 构建树形结构
     */
    private List<CategoryTreeNode> buildTree(List<Category> allCategories) {
        // 转换为树节点
        List<CategoryTreeNode> allNodes = allCategories.stream()
                .map(CategoryTreeNode::fromCategory)
                .collect(Collectors.toList());

        // 构建父子关系
        Map<Long, CategoryTreeNode> nodeMap = allNodes.stream()
                .collect(Collectors.toMap(CategoryTreeNode::getId, node -> node));

        List<CategoryTreeNode> rootNodes = new ArrayList<>();

        for (CategoryTreeNode node : allNodes) {
            Long parentId = node.getParentId();
            if (parentId == null || parentId == 0L) {
                rootNodes.add(node);
            } else {
                CategoryTreeNode parent = nodeMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(node);
                } else {
                    // 如果父节点不存在，也作为根节点
                    rootNodes.add(node);
                }
            }
        }

        // 对子节点按sortOrder排序
        for (CategoryTreeNode node : allNodes) {
            if (!node.getChildren().isEmpty()) {
                node.getChildren().sort(Comparator.comparing(CategoryTreeNode::getSortOrder));
            }
        }

        // 对根节点排序
        rootNodes.sort(Comparator.comparing(CategoryTreeNode::getSortOrder));

        return rootNodes;
    }

    /**
     * 递归获取所有子分类ID
     */
    private void getAllChildrenIds(Long parentId, List<Long> result) {
        QueryWrapper<Category> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("parent_id", parentId)
                .eq("delete_flag", CategoryConstants.DELETE_FLAG_NO);
        List<Category> children = list(queryWrapper);

        for (Category child : children) {
            result.add(child.getId());
            getAllChildrenIds(child.getId(), result);
        }
    }

}
