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

        // 首先检查是否为特定的类别关键词，进行直接映射
        List<Long> directMappedIds = getDirectCategoryIds(keyword);
        if (!directMappedIds.isEmpty()) {
            return directMappedIds;
        }

        // 如果不是特定的类别，则执行原有的查询逻辑
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

    // 新增方法：根据特定关键词直接返回对应的category_id
    private List<Long> getDirectCategoryIds(String keyword) {
        List<Long> categoryIds = new ArrayList<>();

        switch (keyword) {
            case "特色女装":
            case "女装":
            case "女士服装":
                categoryIds.add(23L);
                break;
            case "精品男装":
            case "男装":
            case "男士服装":
                categoryIds.add(24L);
                break;
            case "潮流女鞋":
            case "女鞋":
            case "高跟鞋":
                // 查询包含"高跟鞋"的类别
                List<Category> highHeelCategories = categoryRepository.findByNameContainingAndDeleteFlag("高跟鞋", 0);
                for (Category cat : highHeelCategories) {
                    categoryIds.add(cat.getId());
                }
                break;
            case "品质男鞋":
            case "男鞋":
            case "皮鞋":
                // 查询包含"皮鞋"的类别
                List<Category> leatherShoeCategories = categoryRepository.findByNameContainingAndDeleteFlag("皮鞋", 0);
                for (Category cat : leatherShoeCategories) {
                    categoryIds.add(cat.getId());
                }
                break;
            case "功能箱包":
            case "箱包":
            case "钱包":
                // 查询包含"钱包"的类别
                List<Category> walletCategories = categoryRepository.findByNameContainingAndDeleteFlag("钱包", 0);
                for (Category cat : walletCategories) {
                    categoryIds.add(cat.getId());
                }
                break;
            case "黄金首饰":
            case "黄金":
            case "首饰":
                categoryIds.add(31L);
                break;
            case "钻石彩宝":
            case "钻石":
            case "彩宝":
                // 查询包含"钻石"的类别
                List<Category> diamondCategories = categoryRepository.findByNameContainingAndDeleteFlag("钻石", 0);
                for (Category cat : diamondCategories) {
                    categoryIds.add(cat.getId());
                }
                break;
            case "面部护理":
            case "面部":
            case "护肤":
                categoryIds.add(39L);
                break;
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
