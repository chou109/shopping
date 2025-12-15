package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.ProductDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
/**
 * 商品明细Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Mapper
public interface ProductDetailMapper extends BaseMapper<ProductDetail>
{
    /**
     * 查询商品明细
     * 
     * @param id 商品明细主键
     * @return 商品明细
     */
    public ProductDetail selectProductDetailById(Long id);

    /**
     * 查询商品明细列表
     * 
     * @param productDetail 商品明细
     * @return 商品明细集合
     */
    public List<ProductDetail> selectProductDetailList(ProductDetail productDetail);

    /**
     * 新增商品明细
     * 
     * @param productDetail 商品明细
     * @return 结果
     */
    public int insertProductDetail(ProductDetail productDetail);

    /**
     * 修改商品明细
     * 
     * @param productDetail 商品明细
     * @return 结果
     */
    public int updateProductDetail(ProductDetail productDetail);

    /**
     * 删除商品明细
     * 
     * @param id 商品明细主键
     * @return 结果
     */
    public int deleteProductDetailById(Long id);

    /**
     * 批量删除商品明细
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteProductDetailByIds(Long[] ids);
}
