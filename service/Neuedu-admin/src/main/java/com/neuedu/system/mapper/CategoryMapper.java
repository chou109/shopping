package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.Category;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 类目Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category>
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
     * 删除类目
     * 
     * @param id 类目主键
     * @return 结果
     */
    public int deleteCategoryById(Long id);

    /**
     * 批量删除类目
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCategoryByIds(Long[] ids);


    // ================ 新增查询方法 ================

    /**
     * 查询子分类列表
     */
    List<Category> selectChildrenByParentId(@Param("parentId") Long parentId);

    /**
     * 查询分类树节点列表
     */
    List<Category> selectCategoryTreeNodes(@Param("statusEnabled") Integer statusEnabled,
                                           @Param("deleteFlagNo") Integer deleteFlagNo);

    /**
     * 统计分类下的商品数量
     */
    int countProductsByCategoryId(@Param("categoryId") Long categoryId,
                                  @Param("statusEnabled") Integer statusEnabled,
                                  @Param("deleteFlagNo") Integer deleteFlagNo);
}
