package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.Cart;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
/**
 * 购物车Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-10
 */
@Mapper
public interface CartMapper extends BaseMapper<Cart>
{
    /**
     * 查询购物车
     * 
     * @param id 购物车主键
     * @return 购物车
     */
    public Cart selectCartById(Long id);

    /**
     * 查询购物车列表
     * 
     * @param cart 购物车
     * @return 购物车集合
     */

    /**
     * 根据用户ID查询购物车列表
     *
     * @param userId 用户ID
     * @return 购物车集合
     */
    public List<Cart> selectCartByUserId(Long userId);


    public List<Cart> selectCartList(Cart cart);

    /**
     * 根据用户ID和商品ID查询购物车
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 购物车
     */
    public Cart selectCartByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);


    /**
     * 新增购物车
     * 
     * @param cart 购物车
     * @return 结果
     */
    public int insertCart(Cart cart);

    /**
     * 修改购物车
     * 
     * @param cart 购物车
     * @return 结果
     */
    public int updateCart(Cart cart);

    /**
     * 删除购物车
     * 
     * @param id 购物车主键
     * @return 结果
     */
    public int deleteCartById(Long id);

    /**
     * 批量删除购物车
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteCartByIds(Long[] ids);
}
