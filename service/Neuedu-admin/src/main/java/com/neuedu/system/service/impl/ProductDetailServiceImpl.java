package com.neuedu.system.service.impl;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.ProductDetailMapper;
import com.neuedu.system.domain.ProductDetail;
import com.neuedu.system.service.IProductDetailService;

/**
 * 商品明细Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductDetailServiceImpl extends ServiceImpl<ProductDetailMapper, ProductDetail> implements IProductDetailService
{
    private final ProductDetailMapper productDetailMapper;

    /**
     * 查询商品明细
     * 
     * @param id 商品明细主键
     * @return 商品明细
     */
    @Override
    public ProductDetail selectProductDetailById(Long id)
    {
        return productDetailMapper.selectProductDetailById(id);
    }

    /**
     * 查询商品明细列表
     * 
     * @param productDetail 商品明细
     * @return 商品明细
     */
    @Override
    public List<ProductDetail> selectProductDetailList(ProductDetail productDetail)
    {
        return productDetailMapper.selectProductDetailList(productDetail);
    }

    /**
     * 新增商品明细
     * 
     * @param productDetail 商品明细
     * @return 结果
     */
    @Override
    public int insertProductDetail(ProductDetail productDetail)
    {
        productDetail.setCreateTime(DateUtils.getNowDate());
        return productDetailMapper.insertProductDetail(productDetail);
    }

    /**
     * 修改商品明细
     * 
     * @param productDetail 商品明细
     * @return 结果
     */
    @Override
    public int updateProductDetail(ProductDetail productDetail)
    {
        productDetail.setUpdateTime(DateUtils.getNowDate());
        return productDetailMapper.updateProductDetail(productDetail);
    }

    /**
     * 批量删除商品明细
     * 
     * @param ids 需要删除的商品明细主键
     * @return 结果
     */
    @Override
    public int deleteProductDetailByIds(Long[] ids)
    {
        return productDetailMapper.deleteProductDetailByIds(ids);
    }

    /**
     * 删除商品明细信息
     * 
     * @param id 商品明细主键
     * @return 结果
     */
    @Override
    public int deleteProductDetailById(Long id)
    {
        return productDetailMapper.deleteProductDetailById(id);
    }
}
