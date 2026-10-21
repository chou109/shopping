package com.neuedu.system.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.OrderDetail;
import com.neuedu.system.domain.Product;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IOrderInfoService;
import com.neuedu.system.service.IOrderDetailService;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.IShopService;
import com.neuedu.system.service.BlindBoxPrizePolicy;
import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.common.utils.poi.ExcelUtil;
import com.neuedu.common.core.page.TableDataInfo;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

/**
 * 订单Controller
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@RestController
@RequestMapping("/system/orderInfo")
@Tag(name = "【订单】管理")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderInfoController extends BaseController {
    private static final BigDecimal BLIND_BOX_PRICE = new BigDecimal("500");
    private static final Long BLIND_BOX_PRODUCT_ID = 9999L;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ConcurrentMap<Long, Object> BLIND_BOX_LOCKS = new ConcurrentHashMap<>();

    private final IOrderInfoService orderInfoService;

    @Autowired
    private IProductService productService;

    @Autowired
    private IOrderDetailService orderDetailService;

    @Autowired
    private IShopService shopService;

    /**
     * 查询我的订单列表（用户端）
     */
    @Operation(summary = "查询我的订单列表")
    @GetMapping
    public TableDataInfo myOrders(@RequestParam(required = false) Integer status) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return getDataTable(null);
        }

        startPage();
        List<OrderInfo> list = orderInfoService.selectOrderListByUserId(userId, status);
        return getDataTable(list);
    }

    /**
     * 获取我的订单数量
     */
    @Operation(summary = "获取我的订单数量")
    @GetMapping("/my/count")
    public AjaxResult myOrderCount() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return success(0);
        }
        List<OrderInfo> list = orderInfoService.selectOrderListByUserId(userId, null);
        return success(list != null ? list.size() : 0);
    }

    /**
     * 检查用户是否可以购买盲盒
     */
    @Operation(summary = "检查盲盒购买资格")
    @GetMapping("/blind-box/check")
    public AjaxResult checkBlindBoxPurchase() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }
        
        List<OrderDetail> userOrderDetails = orderDetailService.selectOrderDetailByUserId(userId);
        boolean hasPurchasedBlindBox = userOrderDetails != null && userOrderDetails.stream()
            .anyMatch(detail -> detail.getProductName() != null && detail.getProductName().startsWith("【盲盒】"));
        
        if (hasPurchasedBlindBox) {
            return error("每个用户只能购买一次盲盒，您已经购买过了");
        }
        return success("可以购买");
    }

    /** 服务端抽取奖品并创建固定金额盲盒订单。客户端只能提交收货地址。 */
    @Operation(summary = "抽取盲盒并创建订单")
    @Log(title = "盲盒订单", businessType = BusinessType.INSERT)
    @PostMapping("/blind-box/create")
    public AjaxResult createBlindBoxOrder(@RequestParam Long addressId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }
        Object userLock = BLIND_BOX_LOCKS.computeIfAbsent(userId, ignored -> new Object());
        synchronized (userLock) {
            try {
                if (hasPurchasedBlindBox(userId)) {
                    return error("每个用户只能购买一次盲盒，您已经购买过了");
                }
                Product prize = selectBlindBoxPrize();
                if (prize == null) {
                    return error("暂无可抽取的奖品，请稍后重试");
                }
                OrderInfo order = orderInfoService.createDirectOrderWithOptions(
                    userId, prize.getId(), 1, addressId, BLIND_BOX_PRICE, "【盲盒】");
                return success(order);
            } catch (RuntimeException e) {
                return error(e.getMessage());
            }
        }
    }

    private boolean hasPurchasedBlindBox(Long userId) {
        List<OrderDetail> details = orderDetailService.selectOrderDetailByUserId(userId);
        return details != null && details.stream()
            .anyMatch(detail -> detail.getProductName() != null && detail.getProductName().startsWith("【盲盒】"));
    }

    private Product selectBlindBoxPrize() {
        int roll = RANDOM.nextInt(100);
        Long preferredShop = BlindBoxPrizePolicy.preferredShop(roll, RANDOM.nextInt(7));
        List<Long> shopChoices = new ArrayList<>();
        shopChoices.add(preferredShop);
        if (!shopChoices.contains(10L)) shopChoices.add(10L);
        if (!shopChoices.contains(11L)) shopChoices.add(11L);
        for (Long shopId : shopChoices) {
            Product query = eligiblePrizeQuery(shopId);
            Product prize = pickPrize(productService.selectProductList(query));
            if (prize != null) return prize;
        }
        return pickPrize(productService.selectProductList(eligiblePrizeQuery(null)));
    }

    private Product eligiblePrizeQuery(Long shopId) {
        Product query = new Product();
        query.setStatus(1);
        query.setMinStock(1L);
        query.setIsBlindBox(0);
        query.setShopId(shopId);
        return query;
    }

    private Product pickPrize(List<Product> products) {
        if (products == null) return null;
        List<Product> eligible = new ArrayList<>();
        for (Product product : products) {
            if (product != null && product.getId() != null && !BLIND_BOX_PRODUCT_ID.equals(product.getId())
                && Integer.valueOf(1).equals(product.getStatus())
                && (product.getStock() == null || product.getStock() > 0)
                && !Integer.valueOf(1).equals(product.getIsBlindBox())) {
                eligible.add(product);
            }
        }
        return eligible.isEmpty() ? null : eligible.get(RANDOM.nextInt(eligible.size()));
    }

    /**
     * 创建订单（从购物车，用户端）
     */
    @Operation(summary = "创建订单（从购物车）")
    @Log(title = "订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult createOrder(@RequestParam Long addressId) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }

        try {
            OrderInfo orderInfo = orderInfoService.createOrderFromCart(userId, addressId);
            return success(orderInfo);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 直接购买创建订单（单个商品，用户端）
     * 普通商品直接购买；盲盒订单必须使用服务端抽奖接口。
     */
    @Operation(summary = "直接购买创建订单")
    @Log(title = "订单", businessType = BusinessType.INSERT)
    @PostMapping("/direct")
    public AjaxResult createDirectOrder(@RequestBody Map<String, Object> params) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }

        try {
            Long productId = Long.valueOf(params.get("productId").toString());
            Integer quantity = Integer.valueOf(params.get("quantity").toString());
            Long addressId = Long.valueOf(params.get("addressId").toString());
            if (quantity < 1) {
                return error("购买数量必须大于0");
            }
            Object blindBoxParam = params.get("isBlindBox");
            boolean requestedBlindBox = blindBoxParam != null && Boolean.parseBoolean(blindBoxParam.toString());
            if (BLIND_BOX_PRODUCT_ID.equals(productId) || requestedBlindBox) {
                return error("盲盒请通过盲盒页面购买");
            }
            Product product = productService.selectProductById(productId);
            if (product == null || Integer.valueOf(1).equals(product.getIsBlindBox())) {
                return error("该商品不能通过普通购买接口下单");
            }
            OrderInfo orderInfo = orderInfoService.createDirectOrder(userId, productId, quantity, addressId);
            return success(orderInfo);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 获取订单详情（用户端）
     */
    @Operation(summary = "获取订单详情")
    @GetMapping("/{orderNo}")
    public AjaxResult getOrderDetail(@PathVariable String orderNo) {
        Long userId = SecurityUtils.getUserId();
        OrderInfo orderInfo = orderInfoService.selectOrderInfoByOrderNo(orderNo);

        if (orderInfo == null) {
            return error("订单不存在");
        }

        // 验证订单是否属于当前用户
        if (!orderInfo.getUserId().equals(userId)) {
            return error("无权查看此订单");
        }

        return success(orderInfo);
    }

    /**
     * 取消订单（用户端）
     */
    @Operation(summary = "取消订单")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderNo}/cancel")
    public AjaxResult cancelOrder(@PathVariable String orderNo, @RequestParam String cancelReason) {
        Long userId = SecurityUtils.getUserId();
        OrderInfo orderInfo = orderInfoService.selectOrderInfoByOrderNo(orderNo);

        if (orderInfo == null) {
            return error("订单不存在");
        }

        // 验证订单是否属于当前用户
        if (!orderInfo.getUserId().equals(userId)) {
            return error("无权操作此订单");
        }

        try {
            int result = orderInfoService.cancelOrder(orderNo, cancelReason);
            return toAjax(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 支付订单（用户端）
     */
    @Operation(summary = "支付订单")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderNo}/pay")
    public AjaxResult payOrder(@PathVariable String orderNo, @RequestParam Integer paymentType) {
        Long userId = SecurityUtils.getUserId();
        OrderInfo orderInfo = orderInfoService.selectOrderInfoByOrderNo(orderNo);

        if (orderInfo == null) {
            return error("订单不存在");
        }

        // 验证订单是否属于当前用户
        if (!orderInfo.getUserId().equals(userId)) {
            return error("无权操作此订单");
        }

        try {
            int result = orderInfoService.payOrder(orderNo, paymentType);
            return toAjax(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 确认收货（用户端）
     */
    @Operation(summary = "确认收货")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderNo}/confirm-receipt")
    public AjaxResult confirmReceipt(@PathVariable String orderNo) {
        Long userId = SecurityUtils.getUserId();
        OrderInfo orderInfo = orderInfoService.selectOrderInfoByOrderNo(orderNo);

        if (orderInfo == null) {
            return error("订单不存在");
        }

        // 验证订单是否属于当前用户
        if (!orderInfo.getUserId().equals(userId)) {
            return error("无权操作此订单");
        }

        try {
            int result = orderInfoService.confirmReceipt(orderNo);
            return toAjax(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 完成订单（评价后，用户端）
     */
    @Operation(summary = "完成订单")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderNo}/complete")
    public AjaxResult completeOrder(@PathVariable String orderNo) {
        Long userId = SecurityUtils.getUserId();
        OrderInfo orderInfo = orderInfoService.selectOrderInfoByOrderNo(orderNo);

        if (orderInfo == null) {
            return error("订单不存在");
        }

        // 验证订单是否属于当前用户
        if (!orderInfo.getUserId().equals(userId)) {
            return error("无权操作此订单");
        }

        try {
            int result = orderInfoService.completeOrder(orderNo);
            return toAjax(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 申请退款（用户端）
     */
    @Operation(summary = "申请退款")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PostMapping("/apply-refund")
    public AjaxResult applyRefund(@RequestBody Map<String, Object> refundData) {
        Long userId = SecurityUtils.getUserId();
        String orderNo = (String) refundData.get("orderNo");
        OrderInfo orderInfo = orderInfoService.selectOrderInfoByOrderNo(orderNo);

        if (orderInfo == null) {
            return error("订单不存在");
        }

        // 验证订单是否属于当前用户
        if (!orderInfo.getUserId().equals(userId)) {
            return error("无权操作此订单");
        }

        try {
            String reason = (String) refundData.get("reason");
            String description = (String) refundData.get("description");
            String contactPhone = (String) refundData.get("contactPhone");
            
            int result = orderInfoService.applyRefund(orderNo, reason, description, contactPhone);
            return toAjax(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 获取订单统计（用户端）
     */
    @Operation(summary = "获取我的订单统计")
    @GetMapping("/statistics")
    public AjaxResult getMyOrderStatistics() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }

        Map<String, Object> statistics = orderInfoService.getUserOrderStatistics(userId);
        return success(statistics);
    }

    /**
     * 用户删除自己的订单
     */
    @Operation(summary = "用户删除订单")
    @Log(title = "订单", businessType = BusinessType.DELETE)
    @PostMapping("/delete-my-order")
    public AjaxResult userDelete(@RequestParam("id") Long id) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return AjaxResult.error("用户未登录");
        }
        // 验证订单属于当前用户
        OrderInfo order = orderInfoService.selectOrderInfoById(id);
        if (order == null) {
            return AjaxResult.error("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            return AjaxResult.error("无权删除此订单");
        }
        return toAjax(orderInfoService.deleteOrderInfoById(id));
    }

    /**
     * 商家端发货订单
     */
    @Operation(summary = "商家端发货订单")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderNo}/merchant-ship")
    public AjaxResult merchantShipOrder(@PathVariable String orderNo, @RequestBody Map<String, String> shippingInfo) {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return error("用户未登录");
        }

        Shop shop = shopService.selectShopByUserId(userId);
        if (shop == null) {
            return error("当前账号没有可管理的店铺");
        }

        OrderInfo orderInfo = orderInfoService.selectOrderInfoByOrderNo(orderNo);
        if (orderInfo == null) {
            return error("订单不存在");
        }

        // 验证订单中的每件商品都属于当前店铺。
        List<OrderDetail> orderDetails = orderInfo.getOrderDetails();
        if (orderDetails == null || orderDetails.isEmpty()) {
            return error("订单明细不存在");
        }
        
        for (OrderDetail detail : orderDetails) {
            Product product = productService.selectProductById(detail.getProductId());
            if (product == null || product.getShopId() == null || !shop.getId().equals(product.getShopId())) {
                return error("订单包含其他店铺商品，无法由当前店铺发货");
            }
        }

        // 验证订单状态是否为待发货
        if (!orderInfo.getStatus().equals(OrderStatusConstants.WAIT_SHIP)) {
            return error("订单状态不正确，无法发货");
        }

        try {
            int result = orderInfoService.shipOrder(orderNo, shippingInfo);
            return toAjax(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

}

/**
 * 订单管理Controller（管理员端）
 */
@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "【订单】管理员端")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
class OrderInfoAdminController extends BaseController
{
    private final IOrderInfoService orderInfoService;
    
    @Autowired
    private IProductService productService;
    
    @Autowired
    private com.neuedu.system.service.IShopService shopService;

    @Operation(summary = "查询订单列表（管理员）")
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(OrderInfo orderInfo)
    {
        Long currentUserId = SecurityUtils.getUserId();
        boolean isAdmin = (currentUserId != null && currentUserId == 1L);
        
        if (isAdmin) {
            // 超级管理员查看所有订单
            startPage();
            List<OrderInfo> list = orderInfoService.selectOrderInfoList(orderInfo);
            return getDataTable(list);
        } else {
            // 商家只能查看自己店铺的订单
            // 先获取店铺和商品信息（不受分页影响）
            com.neuedu.system.domain.Shop myShop = shopService.selectShopByUserId(currentUserId);
            if (myShop == null) {
                return getDataTable(new java.util.ArrayList<>());
            }
            
            // 获取商家店铺的所有商品ID
            Product productQuery = new Product();
            productQuery.setShopId(myShop.getId());
            List<Product> myProducts = productService.selectProductList(productQuery);
            
            if (myProducts == null || myProducts.isEmpty()) {
                return getDataTable(new java.util.ArrayList<>());
            }
            
            java.util.Set<Long> myProductIds = new java.util.HashSet<>();
            for (Product p : myProducts) {
                myProductIds.add(p.getId());
            }
            
            // 查询所有订单（不分页），然后过滤出包含商家商品的订单
            List<OrderInfo> allOrders = orderInfoService.selectOrderInfoList(orderInfo);
            List<OrderInfo> myOrders = new java.util.ArrayList<>();
            
            if (allOrders != null) {
                for (OrderInfo order : allOrders) {
                    if (order.getOrderDetails() != null) {
                        for (OrderDetail detail : order.getOrderDetails()) {
                            if (myProductIds.contains(detail.getProductId())) {
                                myOrders.add(order);
                                break;
                            }
                        }
                    }
                }
            }
            
            // 手动构建分页结果
            TableDataInfo rspData = new TableDataInfo();
            rspData.setCode(com.neuedu.common.constant.HttpStatus.SUCCESS);
            rspData.setMsg("查询成功");
            rspData.setRows(myOrders);
            rspData.setTotal(myOrders.size());
            return rspData;
        }
    }

    /**
     * 导出订单列表
     */
    @Operation(summary = "导出订单列表")
    @PreAuthorize("@ss.hasPermi('system:info:export')")
    @Log(title = "订单", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OrderInfo orderInfo)
    {
        List<OrderInfo> list = orderInfoService.selectOrderInfoList(orderInfo);
        ExcelUtil<OrderInfo> util = new ExcelUtil<OrderInfo>(OrderInfo.class);
        util.exportExcel(response, list, "订单数据");
    }

    /**
     * 根据订单号获取订单详细信息（管理员端）
     */
    @Operation(summary = "根据订单号获取订单详细信息（管理员）")
    @PreAuthorize("@ss.hasPermi('system:order:query')")
    @GetMapping(value = "/by-order-no/{orderNo}")
    public AjaxResult getInfoByOrderNo(@PathVariable String orderNo)
    {
        return success(orderInfoService.selectOrderInfoByOrderNo(orderNo));
    }

    /**
     * 获取订单统计信息（管理员端）
     */
    @Operation(summary = "获取订单统计信息（管理员）")
    @PreAuthorize("@ss.hasPermi('system:order:query')")
    @GetMapping("/statistics")
    public AjaxResult getStatistics(@RequestParam(required = false) String startTime,
                                    @RequestParam(required = false) String endTime)
    {
        Map<String, Object> statistics = orderInfoService.getOrderStatistics(startTime, endTime);
        return success(statistics);
    }

    /**
     * 处理退款（管理员端）
     */
    @Operation(summary = "处理退款（管理员）")
    @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/refund/{refundNo}/process")
    public AjaxResult processRefund(@PathVariable String refundNo,
                                    @RequestParam Integer refundStatus,
                                    @RequestParam(required = false) String auditOpinion)
    {
        try {
            int result = orderInfoService.processRefund(refundNo, refundStatus, auditOpinion);
            return toAjax(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 取消订单（管理员端）
     */
    @Operation(summary = "取消订单（管理员）")
    @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderNo}/admin-cancel")
    public AjaxResult adminCancelOrder(@PathVariable String orderNo, @RequestParam String cancelReason)
    {
        try {
            int result = orderInfoService.cancelOrder(orderNo, cancelReason);
            return toAjax(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 发货（管理员端）
     */
    @Operation(summary = "订单发货（管理员）")
    @PreAuthorize("@ss.hasAnyPermi('system:info:edit,system:order:edit')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderNo}/ship")
    public AjaxResult shipOrder(@PathVariable String orderNo, @RequestBody Map<String, String> shippingInfo)
    {
        Long currentUserId = SecurityUtils.getUserId();
        if (currentUserId == null) {
            return error("用户未登录");
        }
        if (currentUserId != 1L) {
            com.neuedu.system.domain.Shop shop = shopService.selectShopByUserId(currentUserId);
            OrderInfo order = orderInfoService.selectOrderInfoByOrderNo(orderNo);
            if (shop == null || order == null || order.getOrderDetails() == null || order.getOrderDetails().isEmpty()) {
                return error("无权操作此订单");
            }
            for (OrderDetail detail : order.getOrderDetails()) {
                Product product = productService.selectProductById(detail.getProductId());
                if (product == null || !shop.getId().equals(product.getShopId())) {
                    return error("订单包含其他店铺商品，无法由当前店铺发货");
                }
            }
        }
        try {
            int result = orderInfoService.shipOrder(orderNo, shippingInfo);
            return toAjax(result);
        } catch (RuntimeException e) {
            return error(e.getMessage());
        }
    }

    /**
     * 获取订单详细信息
     */
    @Operation(summary = "获取订单详细信息")
    @PreAuthorize("@ss.hasPermi('system:info:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(orderInfoService.selectOrderInfoById(id));
    }

    /**
     * 自动取消超时订单（管理员端）
     */
    @Operation(summary = "自动取消超时订单（管理员）")
    @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PostMapping("/auto-cancel")
    public AjaxResult autoCancelExpiredOrders()
    {
        int count = orderInfoService.autoCancelExpiredOrders();
        return success("成功取消 " + count + " 个超时订单");
    }

    /**
     * 自动确认收货（管理员端）
     */
    @Operation(summary = "自动确认收货（管理员）")
    @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PostMapping("/auto-confirm")
    public AjaxResult autoConfirmReceipt()
    {
        int count = orderInfoService.autoConfirmReceipt();
        return success("成功确认 " + count + " 个订单收货");
    }


    /**
     * 新增订单
     */
    @Operation(summary = "新增订单")
    @PreAuthorize("@ss.hasPermi('system:info:add')")
    @Log(title = "订单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OrderInfo orderInfo)
    {
        return toAjax(orderInfoService.insertOrderInfo(orderInfo));
    }

    /**
     * 修改订单
     */
    @Operation(summary = "修改订单")
    @PreAuthorize("@ss.hasPermi('system:info:edit')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OrderInfo orderInfo)
    {
        return toAjax(orderInfoService.updateOrderInfo(orderInfo));
    }

    /**
     * 删除订单（管理员）
     */
    @Operation(summary = "删除订单")
    @PreAuthorize("@ss.hasPermi('system:info:remove')")
    @Log(title = "订单", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable( name = "ids" ) Long[] ids) 
    {
        return toAjax(orderInfoService.deleteOrderInfoByIds(ids));
    }
}
