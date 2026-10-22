package com.neuedu.system.controller;

import com.neuedu.common.annotation.Log;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.core.page.TableDataInfo;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.common.utils.SecurityUtils;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.system.domain.Cart;
import com.neuedu.system.service.ICartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 购物车Controller
 * 
 * @author ruoyi
 * @date 2025-12-10
 */
@RestController
@RequestMapping("/system/cart")
@Tag(name = "【购物车】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CartController extends BaseController
{
    private final ICartService cartService;

    /**
     * 查询购物车列表
     */
    @Operation(summary = "查询购物车列表")
    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping("/list")
    public AjaxResult  list()
    {
        // 获取当前登录用户ID
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }
        Cart cart = new Cart();
        cart.setUserId(userId);
        startPage();
        List<Cart> list = cartService.selectCartList(cart);
        return success(list);
    }

    /**
     * 导出购物车列表
     */
    @Operation(summary = "导出购物车列表")
    @PreAuthorize("@ss.hasPermi('system:cart:export')")
    @Log(title = "购物车", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Cart cart)
    {
        List<Cart> list = cartService.selectCartList(cart);
        ExcelUtil<Cart> util = new ExcelUtil<Cart>(Cart.class);
        util.exportExcel(response, list, "购物车数据");
    }

    /**
     * 获取购物车详细信息
     */
    @Operation(summary = "获取购物车详细信息")
    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        Cart cart = cartService.selectCartById(id);
        Long currentUserId = SecurityUtils.getUserId();
        if (cart != null && !SecurityUtils.isAdmin(currentUserId)
            && !currentUserId.equals(cart.getUserId())) {
            return error("无权查看此购物车");
        }
        return success(cart);
    }

    /**
     * 新增购物车
     */
    @Operation(summary = "新增购物车")
    @PreAuthorize("@ss.hasRole('common')")
    @Log(title = "购物车", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Cart cart)
    {
        // 设置当前用户ID
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }
        
        // 盲盒商品（ID=9999）不允许加入购物车
        if (cart.getProductId() != null && cart.getProductId() == 9999) {
            return error("盲盒商品不支持加入购物车，请直接购买");
        }
        
        cart.setUserId(userId);

        // 设置创建人
        cart.setCreateUser(userId);

        return toAjax(cartService.insertCart(cart));
    }

    /**
     * 修改购物车(修改数量)
     */
    @Operation(summary = "修改购物车")
    @PreAuthorize("@ss.hasRole('common')")
    @Log(title = "购物车", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}")
    public AjaxResult edit(@PathVariable Long id, @RequestBody Cart cart)
    {
        Cart existingCart = cartService.selectCartById(id);
        Long currentUserId = SecurityUtils.getUserId();
        if (existingCart == null)
        {
            return error("购物车不存在");
        }
        if (currentUserId == null || (!SecurityUtils.isAdmin(currentUserId)
                && !currentUserId.equals(existingCart.getUserId())))
        {
            return error("无权修改此购物车");
        }
        cart.setId(id);
        // 购物车所有者、商品和活动关系由服务端既有记录决定，不能从请求体改写。
        cart.setUserId(existingCart.getUserId());
        cart.setProductId(existingCart.getProductId());
        cart.setActivityId(existingCart.getActivityId());
        cart.setActivityName(existingCart.getActivityName());
        cart.setCreateUser(existingCart.getCreateUser());
        cart.setCreateTime(existingCart.getCreateTime());
        cart.setDeleteFlag(existingCart.getDeleteFlag());
        // 设置更新人为当前用户
        cart.setUpdateUser(currentUserId);
        return toAjax(cartService.updateCart(cart));
    }

    /**
     * 删除购物车（删除商品）
     */
    @Operation(summary = "删除购物车")
    @PreAuthorize("@ss.hasRole('common')")
    @Log(title = "购物车", businessType = BusinessType.DELETE)
	@DeleteMapping("/{id}")
    public AjaxResult remove(@PathVariable Long id)
    {
        Long userId = SecurityUtils.getUserId();
        int result = SecurityUtils.isAdmin(userId)
            ? cartService.deleteCartById(id)
            : cartService.deleteCartByIdForUser(id, userId);
        return toAjax(result);
    }


    /**
     * 批量删除购物车
     */
    @Operation(summary = "批量删除购物车商品")
    @PreAuthorize("@ss.hasRole('common')")
    @Log(title = "购物车", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch")
    public AjaxResult removeBatch(@RequestBody Long[] ids)
    {
        Long userId = SecurityUtils.getUserId();
        int result = SecurityUtils.isAdmin(userId)
            ? cartService.deleteCartByIds(ids)
            : cartService.deleteCartByIdsForUser(ids, userId);
        return toAjax(result);
    }

    /**
     * 清空当前用户购物车
     */
    @Operation(summary = "清空购物车")
    @PreAuthorize("@ss.hasRole('common')")
    @Log(title = "购物车", businessType = BusinessType.DELETE)
    @DeleteMapping("/clear")
    public AjaxResult clear()
    {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }
        return toAjax(cartService.clearCartByUserId(userId));
    }

    /**
     * 获取当前用户购物车数量
     */
    @Operation(summary = "获取购物车商品数量")
    @PreAuthorize("@ss.hasRole('common')")
    @GetMapping("/count")
    public AjaxResult count()
    {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        List<Cart> list = cartService.selectCartList(cart);

        // 计算总数量和总金额
        int totalQuantity = list.stream()
                .mapToInt(Cart::getQuantity)
                .sum();

        BigDecimal totalAmount = list.stream()
                .map(cartItem -> cartItem.getProductTotalPrice() != null ?
                        cartItem.getProductTotalPrice() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return success(new HashMap<String, Object>() {{
            put("count", list.size());
            put("totalQuantity", totalQuantity);
            put("totalAmount", totalAmount);
        }});
    }
    /**
     * 批量选择/取消选择商品
     */
    @Operation(summary = "批量选择商品")
    @PreAuthorize("@ss.hasRole('common')")
    @Log(title = "购物车", businessType = BusinessType.UPDATE)
    @PutMapping("/batch-select")
    public AjaxResult batchSelect(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }

        Integer selected = (Integer) params.get("selected");
        List<Cart> userCarts = cartService.selectCartByUserId(userId);

        for (Cart cart : userCarts) {
            cart.setSelected(selected);
            cart.setUpdateUser(userId);
            cartService.updateCart(cart);
        }

        return success();
    }
}
