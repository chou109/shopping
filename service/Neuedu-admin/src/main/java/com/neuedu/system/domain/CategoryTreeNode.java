package com.neuedu.system.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 分类树节点
 */
@Data
public class CategoryTreeNode {
    /** 分类ID */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类ID */
    private Long parentId;

    /** 排序 */
    private Long sortOrder;

    /** 状态：1启用 0禁用 */
    private Integer status;

    /** 是否热门：1是 0否 */
    private Integer hotFlag;

    /** 子节点列表 */
    private List<CategoryTreeNode> children = new ArrayList<>();

    /**
     * 从Category实体转换
     */
    public static CategoryTreeNode fromCategory(Category category) {
        CategoryTreeNode node = new CategoryTreeNode();
        node.setId(category.getId());
        node.setName(category.getName());
        node.setParentId(category.getParentId());
        node.setSortOrder(category.getSortOrder());
        node.setStatus(category.getStatus());
        node.setHotFlag(category.getHotFlag());
        return node;
    }
}