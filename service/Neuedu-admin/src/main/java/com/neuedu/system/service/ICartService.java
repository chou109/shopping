package com.neuedu.system.service;

import java.util.List;
import com.neuedu.system.domain.Cart;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 购物车Service接口
 * 
 * @author ruoyi
 * @date 2025-12-10
 */
public interface ICartService extends IService<Cart>
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
    public List<Cart> selectCartList(Cart cart);

    /**
     * 根据用户ID查询购物车列表
     *
     * @param userId 用户ID
     * @return 购物车集合
     */
    List<Cart> selectCartByUserId(Long userId);

    /**
     * 根据用户ID和商品ID查询购物车
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 购物车
     */
    public Cart selectCartByUserIdAndProductId(Long userId, Long productId);

    /**
     * 获取用户选中的购物车商品
     *
     * @param userId 用户ID
     * @return 购物车集合
     */
    public List<Cart> getSelectedCartItems(Long userId);

    /**
     * 清空用户购物车中选中的商品
     *
     * @param userId 用户ID
     * @return 结果
     */
    public int clearSelectedCartItems(Long userId);


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
     * 批量删除购物车
     * 
     * @param ids 需要删除的购物车主键集合
     * @return 结果
     */
    public int deleteCartByIds(Long[] ids);

    /**
     * 删除购物车信息
     * 
     * @param id 购物车主键
     * @return 结果
     */
    public int deleteCartById(Long id);


    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     * @return 结果
     */
    public int clearCartByUserId(Long userId);
}
