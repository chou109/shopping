package com.neuedu.system.controller;

import java.util.List;

import com.neuedu.common.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.neuedu.common.annotation.Log;
import com.neuedu.common.core.controller.BaseController;
import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.enums.BusinessType;
import com.neuedu.system.domain.Product;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.IShopService;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 商品Controller
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@RestController
@RequestMapping("/system/product")
@Tag(name = "【商品】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ProductController extends BaseController
{
    private final IProductService productService;
    private final IShopService shopService;

    /**
     * 查询商品列表
     */
    @Operation(summary = "查询商品列表")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/list")
    public TableDataInfo list(Product product)
    {
        startPage();
        
        // 获取当前登录用户ID
        Long currentUserId = getUserId();

        scopeProductQueryToCurrentShop(product, currentUserId);
        
        List<Product> list = productService.selectProductList(product);
        
        return getDataTable(list);
    }

    /**
     * 导出商品列表
     */
    @Operation(summary = "导出商品列表")
    @PreAuthorize("@ss.hasPermi('system:product:export')")
    @Log(title = "商品", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, Product product)
    {
        scopeProductQueryToCurrentShop(product, SecurityUtils.getUserId());
        List<Product> list = productService.selectProductList(product);
        ExcelUtil<Product> util = new ExcelUtil<Product>(Product.class);
        util.exportExcel(response, list, "商品数据");
    }

    /**
     * 查询热门商品列表
     */
    @Operation(summary = "查询热门商品列表")
    @GetMapping("/hot")
    public AjaxResult hot(@RequestParam(value = "limit", defaultValue = "10") Integer limit)
    {
        List<Product> list = productService.selectHotProductList(limit);
        return success(list);
    }

    /**
     * 根据分类ID查询商品列表
     */
    @Operation(summary = "根据分类ID查询商品")
    @GetMapping("/category/{categoryId}")
    public AjaxResult byCategory(@PathVariable Long categoryId)
    {
        List<Product> list = productService.selectProductByCategoryId(categoryId);
        return success(list);
    }

    /**
     * 根据活动ID查询商品列表
     */
    @Operation(summary = "根据活动ID查询商品")
    @GetMapping("/activity/{activityId}")
    public AjaxResult byActivity(@PathVariable Long activityId)
    {
        List<Product> list = productService.selectProductByActivityId(activityId);
        return success(list);
    }

    /**
     * 搜索商品
     */
    @Operation(summary = "搜索商品")
    @GetMapping("/search")
    public AjaxResult search(@RequestParam String keyword)
    {
        List<Product> list = productService.searchProductByName(keyword);
        return success(list);
    }

    /**
     * 根据店铺ID查询商品列表（公开接口）
     */
    @Operation(summary = "根据店铺ID查询商品")
    @GetMapping("/shop/{shopId}")
    public AjaxResult byShop(@PathVariable Long shopId)
    {
        // 仅公开营业店铺的商品
        Shop shop = shopService.selectShopById(shopId);
        if (shop == null || !Integer.valueOf(1).equals(shop.getStatus())
            || Integer.valueOf(1).equals(shop.getDeleteFlag())) {
            return error("店铺不存在或已停止营业");
        }
        // 通过 shop_id 查询商品
        Product query = new Product();
        query.setShopId(shopId);
        query.setStatus(1); // 只查询上架商品
        List<Product> list = productService.selectProductList(query);
        return success(list);
    }


    /**
     * 获取商品详细信息
     */
    @Operation(summary = "获取商品详细信息")
    @PreAuthorize("@ss.hasPermi('system:product:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        Product product = productService.selectProductById(id);
        if (!canManageProduct(product, SecurityUtils.getUserId())) {
            return error("无权查看此商品");
        }
        return success(product);
    }

    /**
     * 获取上架商品公开信息
     */
    @Operation(summary = "获取商品公开信息")
    @GetMapping(value = "/public/{id}")
    public AjaxResult getPublicInfo(@PathVariable("id") Long id)
    {
        Product product = productService.selectProductById(id);
        if (product == null || !Integer.valueOf(1).equals(product.getStatus())) {
            return error("商品不存在或已下架");
        }
        if (product.getShopId() != null) {
            Shop shop = shopService.selectShopById(product.getShopId());
            if (shop == null || !Integer.valueOf(1).equals(shop.getStatus())
                || Integer.valueOf(1).equals(shop.getDeleteFlag())) {
                return error("商品所属店铺未营业");
            }
        }
        return success(product);
    }

    /**
     * 新增商品
     */
    @Operation(summary = "新增商品")
    @PreAuthorize("@ss.hasPermi('system:product:add')")
    @Log(title = "商品", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody Product product)
    {
        // 获取当前登录用户ID
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return AjaxResult.error("用户未登录");
        }

        if (!SecurityUtils.isAdmin(currentUserId)) {
            Shop myShop = shopService.selectShopByUserId(currentUserId);
            if (myShop == null) return error("当前账号没有可管理的店铺");
            product.setShopId(myShop.getId());
        }
        
        // 设置创建者ID为当前登录用户ID
        product.setCreateUser(currentUserId);
        
        return toAjax(productService.insertProduct(product));
    }

    /**
     * 修改商品
     */
    @Operation(summary = "修改商品")
    @PreAuthorize("@ss.hasPermi('system:product:edit')")
    @Log(title = "商品", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Product product)
    {
        Long currentUserId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(currentUserId)) {
            Product existing = productService.selectProductById(product.getId());
            if (!canManageProduct(existing, currentUserId)) return error("无权修改此商品");
            Shop myShop = shopService.selectShopByUserId(currentUserId);
            product.setShopId(myShop.getId());
            product.setCreateUser(existing.getCreateUser());
        }
        product.setUpdateUser(currentUserId);
        return toAjax(productService.updateProduct(product));
    }

    /**
     * 删除商品
     */
    @Operation(summary = "删除商品")
    @PreAuthorize("@ss.hasPermi('system:product:remove')")
    @Log(title = "商品", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        Long currentUserId = SecurityUtils.getUserId();
        if (!SecurityUtils.isAdmin(currentUserId)) {
            if (ids == null) return error("商品ID不能为空");
            for (Long id : ids) {
                if (!canManageProduct(productService.selectProductById(id), currentUserId)) {
                    return error("选择中包含其他店铺商品，无法删除");
                }
            }
        }
        return toAjax(productService.deleteProductByIds(ids));
    }

    /**
     * 设置商品热门状态
     */
    @Operation(summary = "设置商品热门状态")
    @PreAuthorize("@ss.hasPermi('system:product:edit')")
    @Log(title = "商品", businessType = BusinessType.UPDATE)
    @PutMapping("/{productId}/hot")
    public AjaxResult setHot(@PathVariable Long productId, @RequestParam Integer hotFlag)
    {
        if (!canManageProduct(productService.selectProductById(productId), SecurityUtils.getUserId())) {
            return error("无权修改此商品");
        }
        return toAjax(productService.updateProductHotFlag(productId, hotFlag));
    }

    /**
     * 更新商品库存
     */
    @Operation(summary = "更新商品库存")
    @PreAuthorize("@ss.hasPermi('system:product:edit')")
    @Log(title = "商品", businessType = BusinessType.UPDATE)
    @PutMapping("/{productId}/stock")
    public AjaxResult updateStock(@PathVariable Long productId, @RequestParam Long quantity)
    {
        if (!canManageProduct(productService.selectProductById(productId), SecurityUtils.getUserId())) {
            return error("无权修改此商品库存");
        }
        return toAjax(productService.updateProductStock(productId, quantity));
    }

    /**
     * 上架/下架商品
     */
    @Operation(summary = "上架/下架商品")
    @PreAuthorize("@ss.hasPermi('system:product:edit')")
    @Log(title = "商品", businessType = BusinessType.UPDATE)
    @PutMapping("/{productId}/status")
    public AjaxResult updateStatus(@PathVariable Long productId, @RequestParam Integer status)
    {
        if (!canManageProduct(productService.selectProductById(productId), SecurityUtils.getUserId())) {
            return error("无权修改此商品");
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        return toAjax(productService.updateProduct(product));
    }

    /**
     * 查询商品列表（随机排序）
     */
    @Operation(summary = "查询商品列表（随机排序）")
    @PreAuthorize("@ss.hasPermi('system:product:list')")
    @GetMapping("/list/random")
    public TableDataInfo listRandom(Product product)
    {
        startPage();
        List<Product> list = productService.selectProductListRandom(product);
        return getDataTable(list);
    }

    /** 查询商城首页随机商品，只返回上架商品 */
    @GetMapping("/public/random")
    public TableDataInfo listPublicRandom(Product product)
    {
        startPage();
        product.setStatus(1);
        List<Product> list = productService.selectPublicProductListRandom(product);
        return getDataTable(list);
    }

    void scopeProductQueryToCurrentShop(Product product, Long userId)
    {
        if (SecurityUtils.isAdmin(userId)) return;
        Shop shop = userId == null ? null : shopService.selectShopByUserId(userId);
        product.setShopId(shop == null ? -1L : shop.getId());
    }

    private boolean canManageProduct(Product product, Long userId)
    {
        if (SecurityUtils.isAdmin(userId)) return true;
        if (product == null || product.getShopId() == null || userId == null) return false;
        Shop shop = shopService.selectShopByUserId(userId);
        return shop != null && shop.getId().equals(product.getShopId());
    }
}
