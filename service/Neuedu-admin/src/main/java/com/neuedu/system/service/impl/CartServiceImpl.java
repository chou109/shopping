package com.neuedu.system.service.impl;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.CartMapper;
import com.neuedu.system.domain.Cart;
import com.neuedu.system.service.ICartService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 购物车Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-10
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService
{
    private final CartMapper cartMapper;

    /**
     * 查询购物车
     * 
     * @param id 购物车主键
     * @return 购物车
     */
    @Override
    public Cart selectCartById(Long id)
    {
        return cartMapper.selectCartById(id);
    }

    /**
     * 查询购物车列表
     * 
     * @param cart 购物车
     * @return 购物车
     */
    @Override
    public List<Cart> selectCartList(Cart cart)
    {
        return cartMapper.selectCartList(cart);
    }

    /**
     * 根据用户ID查询购物车列表
     *
     * @param userId 用户ID
     * @return 购物车列表
     */
    @Override
    public List<Cart> selectCartByUserId(Long userId)
    {
        return cartMapper.selectCartByUserId(userId);
    }


    /**
     * 新增购物车
     * 
     * @param cart 购物车
     * @return 结果
     */
    @Override
    @Transactional
    public int insertCart(Cart cart) {
        // 1. 验证用户是否存在（这里应该调用UserService验证，这里简化处理）
        if (cart.getUserId() == null) {
            throw new RuntimeException("用户ID不能为空");
        }

        // 2. 验证商品是否存在（这里应该调用ProductService验证，这里简化处理）
        if (cart.getProductId() == null) {
            throw new RuntimeException("商品ID不能为空");
        }

        // 3. 检查购物车是否已存在相同商品
        Cart existingCart = cartMapper.selectCartByUserIdAndProductId(cart.getUserId(), cart.getProductId());
        if (existingCart != null) {
            // 如果已存在，则更新数量
            existingCart.setQuantity(existingCart.getQuantity() + (cart.getQuantity() != null ? cart.getQuantity() : 1));
            existingCart.setUpdateTime(DateUtils.getNowDate());
            // 重新计算总价
            if (existingCart.getProductUnitPrice() != null && existingCart.getQuantity() != null) {
                existingCart.setProductTotalPrice(
                        existingCart.getProductUnitPrice().multiply(new BigDecimal(existingCart.getQuantity()))
                );
            }
            return cartMapper.updateCart(existingCart);
        }
        // 4. 新增购物车
        cart.setCreateTime(DateUtils.getNowDate());

        // 5. 计算总价
        if (cart.getProductUnitPrice() != null && cart.getQuantity() != null) {
            cart.setProductTotalPrice(
                    cart.getProductUnitPrice().multiply(new BigDecimal(cart.getQuantity()))
            );
        }

        // 6. 设置默认值
        if (cart.getSelected() == null) {
            cart.setSelected(1); // 默认选中
        }
        if (cart.getDeleteFlag() == null) {
            cart.setDeleteFlag(0); // 默认未删除
        }
        if (cart.getQuantity() == null || cart.getQuantity() <= 0) {
            cart.setQuantity(1); // 默认数量为1
        }

        return cartMapper.insertCart(cart);
    }

    /**
     * 修改购物车
     * 
     * @param cart 购物车
     * @return 结果
     */
    @Override
    public int updateCart(Cart cart)
    {
        // 验证购物车是否存在
        Cart existingCart = cartMapper.selectCartById(cart.getId());
        if (existingCart == null) {
            throw new RuntimeException("购物车不存在");
        }

        cart.setUpdateTime(DateUtils.getNowDate());

        // 如果修改了数量或单价，重新计算总价
        if ((cart.getQuantity() != null || cart.getProductUnitPrice() != null)
                && (cart.getQuantity() != null || existingCart.getQuantity() != null)
                && (cart.getProductUnitPrice() != null || existingCart.getProductUnitPrice() != null)) {

            BigDecimal unitPrice = cart.getProductUnitPrice() != null ?
                    cart.getProductUnitPrice() : existingCart.getProductUnitPrice();
            Integer quantity = cart.getQuantity() != null ?
                    cart.getQuantity() : existingCart.getQuantity();

            if (unitPrice != null && quantity != null) {
                cart.setProductTotalPrice(unitPrice.multiply(new BigDecimal(quantity)));
            }
        }

        return cartMapper.updateCart(cart);
    }
    /**
     * 批量删除购物车
     * 
     * @param ids 需要删除的购物车主键
     * @return 结果
     */
    @Override
    public int deleteCartByIds(Long[] ids)
    {
        return cartMapper.deleteCartByIds(ids);
    }

    /**
     * 删除购物车信息
     * 
     * @param id 购物车主键
     * @return 结果
     */
    @Override
    public int deleteCartById(Long id)
    {
        return cartMapper.deleteCartById(id);
    }

    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int clearCartByUserId(Long userId)
    {
        // 这里需要先查询用户的所有购物车，然后批量删除
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        if (cartList.isEmpty()) {
            return 0;
        }

        Long[] ids = cartList.stream()
                .map(Cart::getId)
                .toArray(Long[]::new);

        return deleteCartByIds(ids);
    }
}
