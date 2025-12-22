package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.Product;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 商品Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product>
{
    /**
     * 查询商品
     * 
     * @param id 商品主键
     * @return 商品
     */
    public Product selectProductById(Long id);

    /**
     * 查询商品列表
     * 
     * @param product 商品
     * @return 商品集合
     */
    public List<Product> selectProductList(Product product);

    /**
     * 查询热门商品列表
     *
     * @param limit 限制数量
     * @return 商品集合
     */
    public List<Product> selectHotProductList(@Param("limit") Integer limit);


    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品集合
     */
    public List<Product> selectProductByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 根据活动ID查询商品列表
     *
     * @param activityId 活动ID
     * @return 商品集合
     */
    public List<Product> selectProductByActivityId(@Param("activityId") Long activityId);

    /**
     * 根据商品名称搜索商品
     *
     * @param keyword 搜索关键词
     * @return 商品集合
     */
    public List<Product> searchProductByName(@Param("keyword") String keyword);



    /**
     * 新增商品
     * 
     * @param product 商品
     * @return 结果
     */
    public int insertProduct(Product product);

    /**
     * 修改商品
     * 
     * @param product 商品
     * @return 结果
     */
    public int updateProduct(Product product);

    /**
     * 删除商品
     * 
     * @param id 商品主键
     * @return 结果
     */
    public int deleteProductById(Long id);

    /**
     * 批量删除商品
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProductByIds(Long[] ids);


    /**
     * 更新商品库存
     *
     * @param productId 商品ID
     * @param quantity 数量
     * @return 结果
     */
    public int updateProductStock(@Param("productId") Long productId, @Param("quantity") Long quantity);

    /**
     * 更新商品热门状态
     *
     * @param productId 商品ID
     * @param hotFlag 热门状态
     * @return 结果
     */
    public int updateProductHotFlag(@Param("productId") Long productId, @Param("hotFlag") Integer hotFlag);



}
