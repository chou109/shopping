package com.neuedu.system.service.impl;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.CartMapper;
import com.neuedu.system.domain.Cart;
import com.neuedu.system.service.ICartService;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.domain.Product;
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

    @Autowired
    private IProductService productService;

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
     * 根据用户ID和商品ID查询购物车
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @return 购物车
     */
    @Override
    public Cart selectCartByUserIdAndProductId(Long userId, Long productId)
    {
        return cartMapper.selectCartByUserIdAndProductId(userId, productId);
    }

    /**
     * 获取用户选中的购物车商品
     *
     * @param userId 用户ID
     * @return 购物车集合
     */
    @Override
    public List<Cart> getSelectedCartItems(Long userId)
    {
        List<Cart> userCarts = cartMapper.selectCartByUserId(userId);
        return userCarts.stream()
                .filter(cart -> cart.getSelected() != null && cart.getSelected() == 1)
                .collect(Collectors.toList());
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
        Product product = requirePurchasableProduct(cart.getProductId());
        Integer quantity = cart.getQuantity() == null ? 1 : cart.getQuantity();
        validateQuantity(quantity, product);
        applyProductSnapshot(cart, product, quantity);

        // 3. 检查购物车是否已存在相同商品
        Cart existingCart = cartMapper.selectCartByUserIdAndProductId(cart.getUserId(), cart.getProductId());
        if (existingCart != null) {
            // 如果已存在，则更新数量
            int updatedQuantity = (existingCart.getQuantity() == null ? 0 : existingCart.getQuantity()) + quantity;
            validateQuantity(updatedQuantity, product);
            existingCart.setQuantity(updatedQuantity);
            applyProductSnapshot(existingCart, product, updatedQuantity);
            existingCart.setUpdateTime(DateUtils.getNowDate());
            // 重新计算总价
            return cartMapper.updateCart(existingCart);
        }
        // 4. 新增购物车
        cart.setCreateTime(DateUtils.getNowDate());

        // 5. 计算总价
        applyProductSnapshot(cart, product, quantity);

        // 6. 设置默认值
        if (cart.getSelected() == null) {
            cart.setSelected(0); // 默认不选中
        }
        if (cart.getDeleteFlag() == null) {
            cart.setDeleteFlag(0); // 默认未删除
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
        if (cart.getUpdateUser() != null && !cart.getUpdateUser().equals(existingCart.getUserId())) {
            throw new RuntimeException("无权修改此购物车");
        }

        Product product = requirePurchasableProduct(existingCart.getProductId());
        Integer quantity = cart.getQuantity() != null ? cart.getQuantity() : existingCart.getQuantity();
        validateQuantity(quantity, product);
        applyProductSnapshot(cart, product, quantity);

        cart.setUpdateTime(DateUtils.getNowDate());

        return cartMapper.updateCart(cart);
    }

    private Product requirePurchasableProduct(Long productId) {
        Product product = productService.selectProductById(productId);
        if (product == null || !Integer.valueOf(1).equals(product.getStatus())) {
            throw new RuntimeException("商品不存在或已下架");
        }
        if (Integer.valueOf(1).equals(product.getIsBlindBox()) || Long.valueOf(9999L).equals(product.getId())) {
            throw new RuntimeException("盲盒商品请通过盲盒页面购买");
        }
        if (product.getPrice() == null || product.getPrice().signum() < 0) {
            throw new RuntimeException("商品价格无效");
        }
        return product;
    }

    private void validateQuantity(Integer quantity, Product product) {
        if (quantity == null || quantity < 1) {
            throw new RuntimeException("商品数量必须大于0");
        }
        if (product.getStock() == null || quantity > product.getStock()) {
            throw new RuntimeException("商品库存不足");
        }
    }

    private void applyProductSnapshot(Cart cart, Product product, Integer quantity) {
        cart.setProductName(product.getName());
        cart.setProductSubtitle(product.getSubTitle());
        cart.setProductMainImage(product.getMainImage());
        cart.setProductUnitPrice(product.getPrice());
        cart.setQuantity(quantity);
        cart.setProductTotalPrice(product.getPrice().multiply(new BigDecimal(quantity)));
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

    @Override
    public int deleteCartByIdsForUser(Long[] ids, Long userId)
    {
        if (userId == null || ids == null || ids.length == 0) return 0;
        return cartMapper.deleteCartByIdsForUser(ids, userId);
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

    @Override
    public int deleteCartByIdForUser(Long id, Long userId)
    {
        if (id == null || userId == null) return 0;
        return cartMapper.deleteCartByIdForUser(id, userId);
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

    /**
     * 清空用户选中的购物车商品
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    @Transactional
    public int clearSelectedCartItems(Long userId)
    {
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        if (cartList.isEmpty()) {
            return 0;
        }

        // 获取选中的购物车商品ID
        List<Long> selectedIds = cartList.stream()
                .filter(cart -> cart.getSelected() != null && cart.getSelected() == 1)
                .map(Cart::getId)
                .collect(Collectors.toList());

        if (selectedIds.isEmpty()) {
            return 0;
        }

        Long[] ids = selectedIds.toArray(new Long[0]);
        return deleteCartByIds(ids);
    }
}
