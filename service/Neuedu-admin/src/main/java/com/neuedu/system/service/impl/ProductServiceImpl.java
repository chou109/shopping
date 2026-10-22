package com.neuedu.system.service.impl;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import com.neuedu.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.ProductMapper;
import com.neuedu.system.domain.Product;
import com.neuedu.system.service.IProductService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 商品Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService
{
    private final ProductMapper productMapper;

    /**
     * 查询商品
     * 
     * @param id 商品主键
     * @return 商品
     */
    @Override
    public Product selectProductById(Long id)
    {
        return productMapper.selectProductById(id);
    }

    /**
     * 查询商品列表
     * 
     * @param product 商品
     * @return 商品
     */
    @Override
    public List<Product> selectProductList(Product product)
    {
        return productMapper.selectProductList(product);
    }

    @Override
    public List<Product> selectPublicProductList(Product product)
    {
        return productMapper.selectPublicProductList(product);
    }

    /**
     * 查询热门商品列表
     *
     * @param limit 限制数量
     * @return 热门商品列表
     */
    @Override
    public List<Product> selectHotProductList(Integer limit)
    {
        return productMapper.selectHotProductList(limit);
    }

    /**
     * 根据分类ID查询商品列表
     *
     * @param categoryId 分类ID
     * @return 商品列表
     */
    @Override
    public List<Product> selectProductByCategoryId(Long categoryId)
    {
        return productMapper.selectProductByCategoryId(categoryId);
    }

    /**
     * 根据活动ID查询商品列表
     *
     * @param activityId 活动ID
     * @return 商品列表
     */
    @Override
    public List<Product> selectProductByActivityId(Long activityId)
    {
        return productMapper.selectProductByActivityId(activityId);
    }

    /**
     * 根据商品名称搜索商品
     *
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    @Override
    public List<Product> searchProductByName(String keyword)
    {
        return productMapper.searchProductByName(keyword);
    }

    @Override
    public List<Product> selectPublicSearchPage(String keyword, Long categoryId)
    {
        return productMapper.selectPublicSearchPage(keyword, categoryId);
    }


    /**
     * 新增商品
     * 
     * @param product 商品
     * @return 结果
     */
    @Override
    public int insertProduct(Product product)
    {
        product.setCreateTime(DateUtils.getNowDate());
        // 设置创建者ID为当前登录用户ID
        product.setCreateUser(SecurityUtils.getUserId());
        // 设置默认值
        if (product.getDeleteFlag() == null) {
            product.setDeleteFlag(0);
        }
        if (product.getStatus() == null) {
            product.setStatus(1); // 默认上架
        }
        if (product.getHotFlag() == null) {
            product.setHotFlag(0); // 默认非热门
        }
        if (product.getStock() == null) {
            product.setStock(0L); // 默认库存为0
        }
        if (product.getPrice() == null) {
            product.setPrice(java.math.BigDecimal.ZERO); // 默认价格为0
        }
        return productMapper.insertProduct(product);
    }

    /**
     * 修改商品
     * 
     * @param product 商品
     * @return 结果
     */
    @Override
    public int updateProduct(Product product)
    {
        product.setUpdateTime(DateUtils.getNowDate());
        return productMapper.updateProduct(product);
    }

    /**
     * 批量删除商品
     * 
     * @param ids 需要删除的商品主键
     * @return 结果
     */
    @Override
    public int deleteProductByIds(Long[] ids)
    {
        return productMapper.deleteProductByIds(ids);
    }

    /**
     * 删除商品信息
     * 
     * @param id 商品主键
     * @return 结果
     */
    @Override
    public int deleteProductById(Long id)
    {
        return productMapper.deleteProductById(id);
    }


    /**
     * 更新商品库存
     *
     * @param productId 商品ID
     * @param quantity 数量（正数增加，负数减少）
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProductStock(Long productId, Long quantity)
    {
        if (productId == null || quantity == null) {
            throw new RuntimeException("商品和库存变更数量不能为空");
        }

        // 扣减条件与更新在同一条 SQL 中执行，避免并发下单基于过期库存重复通过检查。
        int result = productMapper.updateProductStock(productId, quantity);
        if (quantity < 0 && result == 0) {
            throw new RuntimeException("库存不足或商品不存在");
        }
        return result;
    }
    /**
     * 设置商品热门状态
     *
     * @param productId 商品ID
     * @param hotFlag 热门状态 1:热门 0:非热门
     * @return 结果
     */
    @Override
    @Transactional
    public int updateProductHotFlag(Long productId, Integer hotFlag)
    {
        if (hotFlag != 0 && hotFlag != 1) {
            throw new RuntimeException("热门状态值不正确，只能是0或1");
        }
        return productMapper.updateProductHotFlag(productId, hotFlag);
    }

    /**
     * 查询商品列表（随机排序）
     *
     * @param product 商品
     * @return 商品
     */
    @Override
    public List<Product> selectProductListRandom(Product product)
    {
        List<Product> list = productMapper.selectProductList(product);
        // 使用 Collections.shuffle 进行随机排序
        Collections.shuffle(list);
        return list;
    }

    /** 查询消费者端可见商品并随机排序 */
    @Override
    public List<Product> selectPublicProductListRandom(Product product)
    {
        List<Product> list = productMapper.selectPublicProductList(product);
        Collections.shuffle(list);
        return list;
    }
}



