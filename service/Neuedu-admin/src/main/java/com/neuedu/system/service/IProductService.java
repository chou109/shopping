package com.neuedu.system.service;

import java.util.List;
import com.neuedu.system.domain.Product;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 商品Service接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
public interface IProductService extends IService<Product>
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

    /** 查询消费者端可见商品（排除停业或已删除店铺） */
    public List<Product> selectPublicProductList(Product product);

    /** 查询消费者端可见商品并随机排序 */
    public List<Product> selectPublicProductListRandom(Product product);

    /**
     * 新增商品
     * 
     * @param product 商品
     * @return 结果
     */
    public int insertProduct(Product product);

    /**
     * 查询热门商品列表
     *
     * @param limit 限制数量
     * @return 热门商品集合
     */
    public List<Product> selectHotProductList(Integer limit);


    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品集合
     */
    public List<Product> selectProductByCategoryId(Long categoryId);

    /**
     * 根据活动ID查询商品列表
     *
     * @param activityId 活动ID
     * @return 商品集合
     */
    public List<Product> selectProductByActivityId(Long activityId);

    /**
     * 根据商品名称搜索商品
     *
     * @param keyword 搜索关键词
     * @return 商品集合
     */
    public List<Product> searchProductByName(String keyword);

    /** 消费者搜索页分页查询 */
    public List<Product> selectPublicSearchPage(String keyword, Long categoryId);



    /**
     * 修改商品
     * 
     * @param product 商品
     * @return 结果
     */
    public int updateProduct(Product product);

    /**
     * 批量删除商品
     * 
     * @param ids 需要删除的商品主键集合
     * @return 结果
     */
    public int deleteProductByIds(Long[] ids);

    /**
     * 删除商品信息
     * 
     * @param id 商品主键
     * @return 结果
     */
    public int deleteProductById(Long id);

    /**
     * 更新商品库存
     *
     * @param productId 商品ID
     * @param quantity 数量（正数增加，负数减少）
     * @return 结果
     */
    public int updateProductStock(Long productId, Long quantity);

    /**
     * 设置商品热门状态
     *
     * @param productId 商品ID
     * @param hotFlag 热门状态 1:热门 0:非热门
     * @return 结果
     */
    public int updateProductHotFlag(Long productId, Integer hotFlag);

    /**
     * 查询商品列表（随机排序）
     *
     * @param product 商品
     * @return 商品集合
     */
    public List<Product> selectProductListRandom(Product product);
}
