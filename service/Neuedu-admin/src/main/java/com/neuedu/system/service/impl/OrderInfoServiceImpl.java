package com.neuedu.system.service.impl;
import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.domain.*;
import com.neuedu.system.service.*;
import com.neuedu.system.util.OrderStatusUtil;
import com.neuedu.common.core.domain.entity.SysUser;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.hibernate.validator.internal.util.stereotypes.Lazy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.OrderInfoMapper;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderInfoServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements IOrderInfoService
{
    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private IOrderDetailService orderDetailService;

    @Autowired
    private ICartService cartService;

    @Autowired
    private IProductService productService;

    @Autowired
    private IUserAddressService userAddressService;

    @Autowired
    private IUserService userService;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired(required = false)
    private IOrderStatusRecordService orderStatusRecordService;

    // 使用 @Lazy 注解延迟加载，解决循环依赖
    @Autowired
    @Lazy
    private IRefundRecordService refundRecordService;

    // 订单超时时间（分钟）
    private static final int ORDER_EXPIRE_MINUTES = 30;


    /**
     * 查询订单
     *
     * @param id 订单主键
     * @return 订单
     */
    @Override
    public OrderInfo selectOrderInfoById(Long id)
    {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoById(id);
        if (orderInfo != null) {
            // 查询订单明细
            List<OrderDetail> orderDetails = orderDetailService.selectOrderDetailByOrderNo(orderInfo.getOrderNo());
            orderInfo.setOrderDetails(orderDetails);
        }
        return orderInfo;
    }

    /**
     * 查询订单列表
     *
     * @param orderInfo 订单
     * @return 订单
     */
    @Override
    public List<OrderInfo> selectOrderInfoList(OrderInfo orderInfo)
    {
        List<OrderInfo> orderInfos = orderInfoMapper.selectOrderInfoList(orderInfo);
        // 为每个订单查询明细
        for (OrderInfo info : orderInfos) {
            List<OrderDetail> orderDetails = orderDetailService.selectOrderDetailByOrderNo(info.getOrderNo());
            info.setOrderDetails(orderDetails);
        }
        return orderInfos;
    }

    /**
     * 根据订单编号查询订单
     *
     * @param orderNo 订单编号
     * @return 订单
     */
    @Override
    public OrderInfo selectOrderInfoByOrderNo(String orderNo)
    {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByOrderNo(orderNo);
        if (orderInfo != null) {
            List<OrderDetail> orderDetails = orderDetailService.selectOrderDetailByOrderNo(orderNo);
            orderInfo.setOrderDetails(orderDetails);
        }
        return orderInfo;
    }

    /**
     * 查询用户订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    @Override
    public List<OrderInfo> selectOrderListByUserId(Long userId, Integer status)
    {
        List<OrderInfo> orderInfos = orderInfoMapper.selectOrderListByUserId(userId, status);
        for (OrderInfo orderInfo : orderInfos) {
            List<OrderDetail> orderDetails = orderDetailService.selectOrderDetailByOrderNo(orderInfo.getOrderNo());
            orderInfo.setOrderDetails(orderDetails);
        }
        return orderInfos;
    }

    /**
     * 创建订单（从购物车）
     *
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 订单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo createOrderFromCart(Long userId, Long addressId)
    {
        // 1. 验证用户
        SysUser sysUser = sysUserService.selectUserById(userId);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 验证地址
        UserAddress address = userAddressService.selectUserAddressById(addressId);
        if (address == null) {
            throw new RuntimeException("收货地址不存在");
        }
        // 检查地址是否属于该用户
        if (!userId.equals(address.getCreateUser())) {
            throw new RuntimeException("收货地址不属于当前用户");
        }

        // 3. 获取用户购物车中选中的商品
        Cart cartQuery = new Cart();
        cartQuery.setUserId(userId);
        cartQuery.setSelected(1); // 选中的商品
        List<Cart> cartItems = cartService.selectCartList(cartQuery);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车中没有选中的商品");
        }

        // 4. 验证商品库存和价格
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (Cart cartItem : cartItems) {
            Product product = productService.selectProductById(cartItem.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在: " + cartItem.getProductName());
            }
            if (product.getStatus() != 1) {
                throw new RuntimeException("商品已下架: " + cartItem.getProductName());
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + cartItem.getProductName());
            }
            // 验证价格（这里可以添加价格验证逻辑）
            totalAmount = totalAmount.add(cartItem.getProductTotalPrice());
        }

        // 5. 生成订单号
        String orderNo = generateOrderNo();

        // 6. 创建订单主信息
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderNo(orderNo);
        orderInfo.setUserId(userId);
        orderInfo.setCreateUser(userId);
        orderInfo.setDeleteFlag(0);
        orderInfo.setStatus(OrderStatusConstants.WAIT_PAY);
        orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_PAY));

        // 设置地址信息
        orderInfo.setAddressId(addressId);
        orderInfo.setReceiveName(address.getReceiveName());
        orderInfo.setReceivePhone(address.getReceivePhone());
        orderInfo.setProvince(address.getProvince());
        orderInfo.setCity(address.getCity());
        orderInfo.setArea(address.getArea());
        orderInfo.setStreet(address.getStreet());
        orderInfo.setPostalCode(address.getPostalCode());

        // 计算总金额（可以加上邮费等）
        orderInfo.setPayment(totalAmount);
        orderInfo.setPostage(BigDecimal.ZERO); // 这里可以计算邮费

        // 设置订单超时时间（30分钟后）
        Date expireTime = DateUtils.addMinutes(DateUtils.getNowDate(), ORDER_EXPIRE_MINUTES);
        orderInfo.setExpireTime(expireTime);

        orderInfo.setCreateTime(DateUtils.getNowDate());
        orderInfoMapper.insertOrderInfo(orderInfo);

        // 7. 创建订单明细
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (Cart cartItem : cartItems) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderNo(orderNo);
            orderDetail.setOrderDetailNo(generateOrderDetailNo());
            orderDetail.setProductId(cartItem.getProductId());
            orderDetail.setProductName(cartItem.getProductName());
            orderDetail.setProductMainImage(cartItem.getProductMainImage());
            orderDetail.setCurrentUnitPrice(cartItem.getProductUnitPrice());
            orderDetail.setQuantity(cartItem.getQuantity().longValue());
            orderDetail.setTotalPrice(cartItem.getProductTotalPrice());
            orderDetail.setUserId(userId);
            orderDetail.setCreateUser(userId);
            orderDetail.setDeleteFlag(0);
            orderDetail.setStatus(OrderStatusConstants.WAIT_PAY);
            orderDetail.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_PAY));

            // 活动信息
            if (cartItem.getActivityId() != null) {
                orderDetail.setActivityId(cartItem.getActivityId());
                orderDetail.setActivityName(cartItem.getActivityName());
                orderDetail.setActivityMainImage(cartItem.getProductMainImage());
            }

            orderDetail.setCreateTime(DateUtils.getNowDate());
            orderDetails.add(orderDetail);

            // 记录状态变更
            if (orderStatusRecordService != null) {
                orderStatusRecordService.recordOrderStatus(
                        orderNo,
                        orderDetail.getOrderDetailNo(),
                        cartItem.getProductId(),
                        cartItem.getProductName(),
                        OrderStatusConstants.WAIT_PAY,
                        OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_PAY),
                        "创建订单"
                );
            }
        }

        // 批量插入订单明细
        if (!orderDetails.isEmpty()) {
            for (OrderDetail detail : orderDetails) {
                orderDetailService.insertOrderDetail(detail);
            }
        }

        // 8. 扣减商品库存（预扣）
        for (Cart cartItem : cartItems) {
            productService.updateProductStock(cartItem.getProductId(), -cartItem.getQuantity().longValue());
        }

        // 9. 清空购物车中选中的商品（这里需要调用购物车服务的方法）
         cartService.clearSelectedCartItems(userId);

        return orderInfo;
    }

    /**
     * 直接购买创建订单（单个商品）
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @param addressId 地址ID
     * @return 订单信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo createDirectOrder(Long userId, Long productId, Integer quantity, Long addressId)
    {
        // 1. 验证用户
        SysUser sysUser = sysUserService.selectUserById(userId);
        if (sysUser == null) {
            throw new RuntimeException("用户不存在");
        }

        // 2. 验证地址
        UserAddress address = userAddressService.selectUserAddressById(addressId);
        if (address == null) {
            throw new RuntimeException("收货地址不存在");
        }
        // 检查地址是否属于该用户
        if (!userId.equals(address.getCreateUser())) {
            throw new RuntimeException("收货地址不属于当前用户");
        }

        // 3. 验证商品
        Product product = productService.selectProductById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架");
        }
        if (product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }

        // 4. 计算总金额
        BigDecimal totalAmount = product.getPrice().multiply(new BigDecimal(quantity));

        // 5. 生成订单号
        String orderNo = generateOrderNo();

        // 6. 创建订单主信息
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderNo(orderNo);
        orderInfo.setUserId(userId);
        orderInfo.setCreateUser(userId);
        orderInfo.setDeleteFlag(0);
        orderInfo.setStatus(OrderStatusConstants.WAIT_PAY);
        orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_PAY));

        // 设置地址信息
        orderInfo.setAddressId(addressId);
        orderInfo.setReceiveName(address.getReceiveName());
        orderInfo.setReceivePhone(address.getReceivePhone());
        orderInfo.setProvince(address.getProvince());
        orderInfo.setCity(address.getCity());
        orderInfo.setArea(address.getArea());
        orderInfo.setStreet(address.getStreet());
        orderInfo.setPostalCode(address.getPostalCode());

        // 计算总金额
        orderInfo.setPayment(totalAmount);
        orderInfo.setPostage(BigDecimal.ZERO);

        // 设置订单超时时间（30分钟后）
        Date expireTime = DateUtils.addMinutes(DateUtils.getNowDate(), ORDER_EXPIRE_MINUTES);
        orderInfo.setExpireTime(expireTime);

        orderInfo.setCreateTime(DateUtils.getNowDate());
        orderInfoMapper.insertOrderInfo(orderInfo);

        // 7. 创建订单明细
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderNo(orderNo);
        orderDetail.setOrderDetailNo(generateOrderDetailNo());
        orderDetail.setProductId(productId);
        orderDetail.setProductName(product.getName());
        orderDetail.setProductMainImage(product.getMainImage());
        orderDetail.setCurrentUnitPrice(product.getPrice());
        orderDetail.setQuantity(quantity.longValue());
        orderDetail.setTotalPrice(totalAmount);
        orderDetail.setUserId(userId);
        orderDetail.setCreateUser(userId);
        orderDetail.setDeleteFlag(0);
        orderDetail.setStatus(OrderStatusConstants.WAIT_PAY);
        orderDetail.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_PAY));
        orderDetail.setCreateTime(DateUtils.getNowDate());

        orderDetailService.insertOrderDetail(orderDetail);

        // 记录状态变更
        if (orderStatusRecordService != null) {
            orderStatusRecordService.recordOrderStatus(
                    orderNo,
                    orderDetail.getOrderDetailNo(),
                    productId,
                    product.getName(),
                    OrderStatusConstants.WAIT_PAY,
                    OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_PAY),
                    "创建订单"
            );
        }

        // 8. 扣减商品库存（预扣）
        productService.updateProductStock(productId, -quantity.longValue());

        // 设置订单明细列表
        List<OrderDetail> orderDetails = new ArrayList<>();
        orderDetails.add(orderDetail);
        orderInfo.setOrderDetails(orderDetails);

        return orderInfo;
    }

    /**
     * 支付订单
     *
     * @param orderNo 订单编号
     * @param paymentType 支付类型
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int payOrder(String orderNo, Integer paymentType)
    {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByOrderNo(orderNo);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态
        if (!OrderStatusConstants.WAIT_PAY.equals(orderInfo.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法支付");
        }

        // 验证订单是否已超时
        if (orderInfo.getExpireTime() != null && orderInfo.getExpireTime().before(DateUtils.getNowDate())) {
            // 自动取消超时订单
            cancelOrder(orderNo, "支付超时");
            throw new RuntimeException("订单已超时，请重新下单");
        }

        // 如果是余额支付，需要扣减用户余额
        if (paymentType.equals(OrderStatusConstants.PAYMENT_BALANCE)) {
            SysUser sysUser = sysUserService.selectUserById(orderInfo.getUserId());
            if (sysUser == null) {
                throw new RuntimeException("用户不存在");
            }
            BigDecimal userBalance = sysUser.getBalance() != null ? sysUser.getBalance() : BigDecimal.ZERO;
            if (userBalance.compareTo(orderInfo.getPayment()) < 0) {
                throw new RuntimeException("余额不足，当前余额" + userBalance + "元，需支付" + orderInfo.getPayment() + "元");
            }
            // 扣减余额
            BigDecimal newBalance = userBalance.subtract(orderInfo.getPayment());
            sysUserService.updateUserBalance(orderInfo.getUserId(), orderInfo.getPayment().negate());
        }

        // 更新订单状态
        int result = orderInfoMapper.payOrder(orderNo, paymentType,
                OrderStatusConstants.getPaymentTypeDesc(paymentType));

        if (result > 0) {
            // 记录状态变更
            if (orderStatusRecordService != null) {
                orderStatusRecordService.recordOrderStatus(
                        orderNo,
                        null,
                        null,
                        null,
                        OrderStatusConstants.PAID,
                        OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.PAID),
                        "订单支付"
                );
            }

            // 更新订单明细状态
            List<OrderDetail> orderDetails = orderDetailService.selectOrderDetailByOrderNo(orderNo);
            for (OrderDetail detail : orderDetails) {
                detail.setStatus(OrderStatusConstants.PAID);
                detail.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.PAID));
                orderDetailService.updateOrderDetail(detail);

                // 记录明细状态变更
                if (orderStatusRecordService != null) {
                    orderStatusRecordService.recordOrderStatus(
                            orderNo,
                            detail.getOrderDetailNo(),
                            detail.getProductId(),
                            detail.getProductName(),
                            OrderStatusConstants.PAID,
                            OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.PAID),
                            "订单支付"
                    );
                }
            }
        }

        return result;
    }

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @param cancelReason 取消原因
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelOrder(String orderNo, String cancelReason)
    {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByOrderNo(orderNo);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态（只有待付款和已付款的订单可以取消）
        if (!OrderStatusConstants.WAIT_PAY.equals(orderInfo.getStatus())
                && !OrderStatusConstants.PAID.equals(orderInfo.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法取消");
        }

        int result = orderInfoMapper.cancelOrder(orderNo, cancelReason);

        if (result > 0) {
            // 恢复商品库存
            List<OrderDetail> orderDetails = orderDetailService.selectOrderDetailByOrderNo(orderNo);
            for (OrderDetail detail : orderDetails) {
                productService.updateProductStock(detail.getProductId(), detail.getQuantity());
            }

            // 如果是已付款订单，需要退款
            if (OrderStatusConstants.PAID.equals(orderInfo.getStatus())) {
                // 退款到原支付方式
                if (orderInfo.getPaymentType().equals(OrderStatusConstants.PAYMENT_BALANCE)) {
                    // 退回余额
                    User user = userService.getById(orderInfo.getUserId());
                    user.setBalance(user.getBalance().add(orderInfo.getPayment()));
                    userService.updateById(user);
                }
                // 其他支付方式的退款逻辑...
            }

            // 记录状态变更
            if (orderStatusRecordService != null) {
                orderStatusRecordService.recordOrderStatus(
                        orderNo,
                        null,
                        null,
                        null,
                        OrderStatusConstants.CANCELLED,
                        OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.CANCELLED),
                        "取消订单：" + cancelReason
                );
            }
        }

        return result;
    }

    /**
     * 发货
     *
     * @param orderNo 订单编号
     * @param shippingInfo 物流信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int shipOrder(String orderNo, Map<String, String> shippingInfo)
    {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByOrderNo(orderNo);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态（只有已付款的订单可以发货）
        if (!OrderStatusConstants.PAID.equals(orderInfo.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法发货");
        }

        // 更新订单状态为已发货
        orderInfo.setStatus(OrderStatusConstants.SHIPPED);
        orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.SHIPPED));
        orderInfo.setShipTime(DateUtils.getNowDate());
        int result = orderInfoMapper.updateOrderInfo(orderInfo);

        if (result > 0 && orderStatusRecordService != null) {
            // 记录状态变更
            orderStatusRecordService.recordOrderStatus(
                    orderNo,
                    null,
                    null,
                    null,
                    OrderStatusConstants.SHIPPED,
                    OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.SHIPPED),
                    "订单发货"
            );
        }

        return result;
    }

    /**
     * 确认收货
     *
     * @param orderNo 订单编号
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmReceipt(String orderNo)
    {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByOrderNo(orderNo);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态（只有已发货的订单可以确认收货）
        if (!OrderStatusConstants.SHIPPED.equals(orderInfo.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法确认收货");
        }

        // 更新订单状态为已完成
        orderInfo.setStatus(OrderStatusConstants.COMPLETED);
        orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.COMPLETED));
        orderInfo.setReceiveTime(DateUtils.getNowDate());
        int result = orderInfoMapper.updateOrderInfo(orderInfo);

        if (result > 0 && orderStatusRecordService != null) {
            // 记录状态变更
            orderStatusRecordService.recordOrderStatus(
                    orderNo,
                    null,
                    null,
                    null,
                    OrderStatusConstants.COMPLETED,
                    OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.COMPLETED),
                    "确认收货"
            );
        }

        return result;
    }

    /**
     * 申请退款
     *
     * @param orderNo 订单编号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int applyRefund(String orderNo, BigDecimal refundAmount, String refundReason)
    {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByOrderNo(orderNo);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态（只有已付款、已发货的订单可以申请退款）
        if (!OrderStatusConstants.PAID.equals(orderInfo.getStatus())
                && !OrderStatusConstants.SHIPPED.equals(orderInfo.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法申请退款");
        }

        // 验证退款金额
        if (refundAmount.compareTo(orderInfo.getPayment()) > 0) {
            throw new RuntimeException("退款金额不能超过订单金额");
        }

        // 调用退款服务
        return refundRecordService.applyRefund(orderNo, orderInfo.getUserId(), refundAmount, refundReason);
    }

    /**
     * 处理退款
     *
     * @param refundNo 退款单号
     * @param refundStatus 退款状态
     * @param auditOpinion 审核意见
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processRefund(String refundNo, Integer refundStatus, String auditOpinion)
    {
        return refundRecordService.processRefund(refundNo, refundStatus, auditOpinion);
    }

    /**
     * 获取订单统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    @Override
    public Map<String, Object> getOrderStatistics(String startTime, String endTime)
    {
        Map<String, Object> statistics = orderInfoMapper.selectOrderStatistics(startTime, endTime, null);
        if (statistics == null) {
            statistics = new HashMap<>();
        }

        // 补充其他统计信息
        List<Map<String, Object>> dailyStats = orderInfoMapper.selectDailyOrderStatistics(startTime, endTime);
        statistics.put("dailyStatistics", dailyStats);

        return statistics;
    }

    /**
     * 自动取消超时订单
     *
     * @return 取消的订单数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoCancelExpiredOrders()
    {
        List<OrderInfo> expiredOrders = orderInfoMapper.selectExpiredOrders();
        int count = 0;

        for (OrderInfo order : expiredOrders) {
            try {
                int result = cancelOrder(order.getOrderNo(), "系统自动取消：支付超时");
                if (result > 0) {
                    count++;
                }
            } catch (Exception e) {
                // 记录日志，继续处理其他订单
                System.err.println("自动取消订单失败：" + order.getOrderNo() + ", " + e.getMessage());
            }
        }

        return count;
    }

    /**
     * 自动确认收货
     *
     * @return 确认的订单数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoConfirmReceipt()
    {
        List<OrderInfo> waitConfirmOrders = orderInfoMapper.selectWaitConfirmOrders();
        int count = 0;

        for (OrderInfo order : waitConfirmOrders) {
            try {
                int result = confirmReceipt(order.getOrderNo());
                if (result > 0) {
                    count++;
                }
            } catch (Exception e) {
                // 记录日志，继续处理其他订单
                System.err.println("自动确认收货失败：" + order.getOrderNo() + ", " + e.getMessage());
            }
        }

        return count;
    }

    /**
     * 新增订单
     * 
     * @param orderInfo 订单
     * @return 结果
     */
    @Override
    public int insertOrderInfo(OrderInfo orderInfo)
    {
        orderInfo.setCreateTime(DateUtils.getNowDate());
        return orderInfoMapper.insertOrderInfo(orderInfo);
    }

    /**
     * 修改订单
     * 
     * @param orderInfo 订单
     * @return 结果
     */
    @Override
    public int updateOrderInfo(OrderInfo orderInfo)
    {
        orderInfo.setUpdateTime(DateUtils.getNowDate());
        return orderInfoMapper.updateOrderInfo(orderInfo);
    }

    /**
     * 批量删除订单
     * 
     * @param ids 需要删除的订单主键
     * @return 结果
     */
    @Override
    public int deleteOrderInfoByIds(Long[] ids)
    {
        return orderInfoMapper.deleteOrderInfoByIds(ids);
    }

    /**
     * 删除订单信息
     * 
     * @param id 订单主键
     * @return 结果
     */
    @Override
    public int deleteOrderInfoById(Long id)
    {
        return orderInfoMapper.deleteOrderInfoById(id);
    }


    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        // 格式：年月日时分秒 + 6位随机数
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        String timeStr = LocalDateTime.now().format(formatter);
        String randomStr = String.format("%06d", (int)(Math.random() * 1000000));
        return "O" + timeStr + randomStr;
    }

    /**
     * 生成订单明细编号
     */
    private String generateOrderDetailNo() {
        // 格式：D + UUID
        return "D" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
