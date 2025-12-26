package com.neuedu.system.demoai.service;

import com.neuedu.system.demoai.entity.Category;
import com.neuedu.system.demoai.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CategoryRepository categoryRepository;

    // 根据关键词获取category_id列表
    public List<Long> getCategoryIdsByKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return new ArrayList<>();
        }

        // 先查询包含关键词的类别
        List<Category> categories = categoryRepository.findByNameContainingAndDeleteFlag(keyword, 0);

        List<Long> categoryIds = new ArrayList<>();
        for (Category category : categories) {
            categoryIds.add(category.getId());

            // 如果是父类别，获取其所有子类别
            if (category.getParentId() == 0) {
                List<Long> childIds = getChildCategoryIds(category.getId());
                categoryIds.addAll(childIds);
            }
        }

        return categoryIds;
    }

    // 获取指定父类别的所有子类别ID
    private List<Long> getChildCategoryIds(Long parentId) {
        List<Category> children = categoryRepository.findByParentIdAndDeleteFlag(parentId, 0);

        List<Long> childIds = new ArrayList<>();
        for (Category child : children) {
            childIds.add(child.getId());

            // 递归获取孙子类别
            List<Long> grandChildIds = getChildCategoryIds(child.getId());
            childIds.addAll(grandChildIds);
        }

        return childIds;
    }

    // 根据多个关键词获取category_id列表
    public List<Long> getCategoryIdsByKeywords(List<String> keywords) {
        List<Long> allCategoryIds = new ArrayList<>();

        for (String keyword : keywords) {
            List<Long> ids = getCategoryIdsByKeyword(keyword);
            allCategoryIds.addAll(ids);
        }

        return allCategoryIds;
    }

    // 获取类别名称
    public String getCategoryNameById(Long categoryId) {
        if (categoryId == null) {
            return "";
        }

        Category category = categoryRepository.findByIdAndDeleteFlag(categoryId, 0);
        return category != null ? category.getName() : "";
    }
}
