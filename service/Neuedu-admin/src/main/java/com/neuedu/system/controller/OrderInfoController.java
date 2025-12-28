package com.neuedu.system.controller;

import java.util.List;
import java.util.Map;

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
import com.neuedu.system.service.IOrderInfoService;
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
    private final IOrderInfoService orderInfoService;


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

        Map<String, Object> statistics = orderInfoService.getOrderStatistics(null, null);
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

    @Operation(summary = "查询订单列表（管理员）")
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(OrderInfo orderInfo)
    {
        // startPage() 会自动从请求中获取 pageNum 和 pageSize
        startPage();
        List<OrderInfo> list = orderInfoService.selectOrderInfoList(orderInfo);

        // getDataTable() 会返回包含 total、rows 等信息的 TableDataInfo
        return getDataTable(list);
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
     * 获取订单详细信息（管理员端）
     */
    @Operation(summary = "获取订单详细信息（管理员）")
    @PreAuthorize("@ss.hasPermi('system:order:query')")
    @GetMapping(value = "/{orderNo}")
    public AjaxResult getInfo(@PathVariable String orderNo)
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
    @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "订单", businessType = BusinessType.UPDATE)
    @PutMapping("/{orderNo}/ship")
    public AjaxResult shipOrder(@PathVariable String orderNo, @RequestBody Map<String, String> shippingInfo)
    {
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
