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
        List<Cart> cartItems = cartService.getSelectedCartItems(userId);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车中没有选中的商品");
        }

        // 4. 验证商品库存和价格
        BigDecimal totalAmount = BigDecimal.ZERO;
        Set<Long> selectedShopIds = new HashSet<>();
        for (Cart cartItem : cartItems) {
            Product product = productService.selectProductById(cartItem.getProductId());
            if (product == null) {
                throw new RuntimeException("商品不存在: " + cartItem.getProductName());
            }
            if (Integer.valueOf(1).equals(product.getIsBlindBox()) || Long.valueOf(9999L).equals(product.getId())) {
                throw new RuntimeException("盲盒商品请通过盲盒页面购买");
            }
            if (product.getStatus() != 1) {
                throw new RuntimeException("商品已下架: " + cartItem.getProductName());
            }
            if (cartItem.getQuantity() == null || cartItem.getQuantity() < 1) {
                throw new RuntimeException("商品数量必须大于0");
            }
            if (product.getStock() == null || product.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + cartItem.getProductName());
            }
            if (product.getPrice() == null) {
                throw new RuntimeException("商品价格无效: " + product.getName());
            }
            selectedShopIds.add(product.getShopId());
            cartItem.setProductName(product.getName());
            cartItem.setProductSubtitle(product.getSubTitle());
            cartItem.setProductMainImage(product.getMainImage());
            cartItem.setProductUnitPrice(product.getPrice());
            cartItem.setProductTotalPrice(product.getPrice().multiply(new BigDecimal(cartItem.getQuantity())));
            totalAmount = totalAmount.add(cartItem.getProductTotalPrice());
        }
        if (selectedShopIds.size() > 1) {
            throw new RuntimeException("购物车所选商品来自多个店铺，请分店选择商品后结算");
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
        if (Integer.valueOf(1).equals(product.getIsBlindBox()) || Long.valueOf(9999L).equals(product.getId())) {
            throw new RuntimeException("盲盒商品请通过盲盒页面购买");
        }
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架");
        }
        if (quantity == null || quantity < 1) {
            throw new RuntimeException("购买数量必须大于0");
        }
        if (product.getStock() == null || product.getStock() < quantity) {
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
     * 直接购买创建订单（支持自定义价格和备注，用于盲盒订单）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderInfo createDirectOrderWithOptions(Long userId, Long productId, Integer quantity, Long addressId, BigDecimal overridePrice, String remark)
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
        if (!userId.equals(address.getCreateUser())) {
            throw new RuntimeException("收货地址不属于当前用户");
        }

        // 3. 验证商品
        Product product = productService.selectProductById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在");
        }
        if (Integer.valueOf(1).equals(product.getIsBlindBox()) || Long.valueOf(9999L).equals(product.getId())) {
            throw new RuntimeException("不能抽取盲盒商品作为盲盒奖品");
        }
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架");
        }
        if (quantity == null || quantity < 1) {
            throw new RuntimeException("购买数量必须大于0");
        }
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }

        // 4. 计算总金额（如果有覆盖价格则使用覆盖价格）
        BigDecimal unitPrice = overridePrice != null ? overridePrice : product.getPrice();
        BigDecimal totalAmount = unitPrice.multiply(new BigDecimal(quantity));

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

        // 如果有盲盒标记，设置为盲盒订单
        if (remark != null && remark.contains("盲盒")) {
            orderInfo.setIsBlindBox(true);
        }

        orderInfo.setCreateTime(DateUtils.getNowDate());
        orderInfoMapper.insertOrderInfo(orderInfo);

        // 7. 创建订单明细
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderNo(orderNo);
        orderDetail.setOrderDetailNo(generateOrderDetailNo());
        orderDetail.setProductId(productId);
        // 如果是盲盒订单，在商品名称前加上标记
        String productName = (remark != null ? remark : "") + product.getName();
        orderDetail.setProductName(productName);
        orderDetail.setProductMainImage(product.getMainImage());
        orderDetail.setCurrentUnitPrice(unitPrice);
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
                    productName,
                    OrderStatusConstants.WAIT_PAY,
                    OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_PAY),
                    "创建订单"
            );
        }

        // 8. 扣减商品库存（预扣）
        productService.updateProductStock(productId, -quantity.longValue());

        // 设置订单明细列表
        List<OrderDetail> orderDetailList = new ArrayList<>();
        orderDetailList.add(orderDetail);
        orderInfo.setOrderDetails(orderDetailList);

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
        if (!OrderStatusConstants.PAYMENT_BALANCE.equals(paymentType)) {
            throw new RuntimeException("该支付方式尚未接入，请使用余额支付");
        }
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
        }

        // 更新订单状态
        int result = orderInfoMapper.payOrder(orderNo, paymentType,
                OrderStatusConstants.getPaymentTypeDesc(paymentType));

        if (result > 0) {
            // 先用订单状态条件更新抢占支付，再由数据库保证余额充足时原子扣款。
            // 扣款失败抛出异常，事务会回滚前面的订单状态更新。
            if (!sysUserService.deductUserBalance(orderInfo.getUserId(), orderInfo.getPayment())) {
                throw new RuntimeException("余额不足，请刷新后重试");
            }

            // 记录状态变更
            if (orderStatusRecordService != null) {
                orderStatusRecordService.recordOrderStatus(
                        orderNo,
                        null,
                        null,
                        null,
                        OrderStatusConstants.WAIT_SHIP,
                        OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_SHIP),
                        "订单支付"
                );
            }

            // 更新订单明细状态
            List<OrderDetail> orderDetails = orderDetailService.selectOrderDetailByOrderNo(orderNo);
            for (OrderDetail detail : orderDetails) {
                detail.setStatus(OrderStatusConstants.WAIT_SHIP);
                detail.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_SHIP));
                orderDetailService.updateOrderDetail(detail);

                // 记录明细状态变更
                if (orderStatusRecordService != null) {
                    orderStatusRecordService.recordOrderStatus(
                            orderNo,
                            detail.getOrderDetailNo(),
                            detail.getProductId(),
                            detail.getProductName(),
                            OrderStatusConstants.WAIT_SHIP,
                            OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_SHIP),
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

        // 普通取消只针对未付款订单；已付款订单必须走带退款保护的退款流程。
        if (!OrderStatusConstants.WAIT_PAY.equals(orderInfo.getStatus())) {
            throw new RuntimeException("仅待付款订单可直接取消，已付款订单请使用退款功能");
        }

        int result = orderInfoMapper.cancelOrder(orderNo, cancelReason);

        if (result > 0) {
            // 恢复商品库存
            List<OrderDetail> orderDetails = orderDetailService.selectOrderDetailByOrderNo(orderNo);
            for (OrderDetail detail : orderDetails) {
                productService.updateProductStock(detail.getProductId(), detail.getQuantity());
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

        // 验证订单状态（只有待发货的订单可以发货）
        if (!OrderStatusConstants.WAIT_SHIP.equals(orderInfo.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法发货");
        }

        // 更新订单状态为待收货
        orderInfo.setStatus(OrderStatusConstants.WAIT_RECEIVE);
        orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_RECEIVE));
        orderInfo.setShipTime(DateUtils.getNowDate());
        int result = orderInfoMapper.updateOrderInfo(orderInfo);

        if (result > 0 && orderStatusRecordService != null) {
            // 记录状态变更
            orderStatusRecordService.recordOrderStatus(
                    orderNo,
                    null,
                    null,
                    null,
                    OrderStatusConstants.WAIT_RECEIVE,
                    OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_RECEIVE),
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

        // 验证订单状态（只有待收货的订单可以确认收货）
        if (!OrderStatusConstants.WAIT_RECEIVE.equals(orderInfo.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法确认收货");
        }

        // 更新订单状态为待评价
        orderInfo.setStatus(OrderStatusConstants.WAIT_REVIEW);
        orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_REVIEW));
        orderInfo.setReceiveTime(DateUtils.getNowDate());
        int result = orderInfoMapper.updateOrderInfo(orderInfo);

        if (result > 0 && orderStatusRecordService != null) {
            // 记录状态变更
            orderStatusRecordService.recordOrderStatus(
                    orderNo,
                    null,
                    null,
                    null,
                    OrderStatusConstants.WAIT_REVIEW,
                    OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.WAIT_REVIEW),
                    "确认收货"
            );
        }

        return result;
    }

    /**
     * 完成订单（评价后）
     *
     * @param orderNo 订单编号
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeOrder(String orderNo)
    {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByOrderNo(orderNo);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        // 验证订单状态（只有待评价的订单可以完成）
        if (!OrderStatusConstants.WAIT_REVIEW.equals(orderInfo.getStatus())) {
            throw new RuntimeException("订单状态不正确，无法完成");
        }

        // 更新订单状态为交易完成
        orderInfo.setStatus(OrderStatusConstants.COMPLETED);
        orderInfo.setStatusDesc(OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.COMPLETED));
        orderInfo.setUpdateTime(DateUtils.getNowDate());
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
                    "评价完成"
            );
        }

        return result;
    }

    /**
     * 申请退款
     *
     * @param orderNo 订单编号
     * @param reason 退款原因
     * @param description 退款说明
     * @param contactPhone 联系电话
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int applyRefund(String orderNo, String reason, String description, String contactPhone)
    {
        OrderInfo orderInfo = orderInfoMapper.selectOrderInfoByOrderNo(orderNo);
        if (orderInfo == null) {
            throw new RuntimeException("订单不存在");
        }

        // 当前实现仅支持取消待发货订单并全额退回余额。
        if (!OrderStatusConstants.WAIT_SHIP.equals(orderInfo.getStatus())) {
            throw new RuntimeException("仅待发货订单可取消并退款");
        }

        // 获取订单明细
        List<OrderDetail> details = orderDetailService.selectOrderDetailByOrderNo(orderNo);
        if (details.isEmpty()) {
            throw new RuntimeException("订单没有明细");
        }

        boolean blindBoxOrder = Boolean.TRUE.equals(orderInfo.getIsBlindBox()) || details.stream().anyMatch(detail ->
            Long.valueOf(9999L).equals(detail.getProductId())
                || (detail.getProductName() != null && detail.getProductName().startsWith("【盲盒")));
        if (blindBoxOrder) {
            throw new RuntimeException("盲盒订单不支持退款");
        }

        // 使用条件更新实现幂等保护，重复或并发请求不能重复退余额。
        int result = orderInfoMapper.cancelOrderForRefund(
            orderNo,
            reason,
            OrderStatusConstants.WAIT_SHIP,
            OrderStatusConstants.CANCELLED,
            OrderStatusConstants.getOrderStatusDesc(OrderStatusConstants.CANCELLED));

        if (result == 0) {
            throw new RuntimeException("订单状态已变化，请刷新后重试");
        }

        // 返还用户余额（使用 sys_user 表）
        sysUserService.updateUserBalance(orderInfo.getUserId(), orderInfo.getPayment());

        // 恢复商品库存
        for (OrderDetail detail : details) {
            productService.updateProductStock(detail.getProductId(), Long.valueOf(Math.abs(detail.getQuantity())));
        }

        return result;
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
        
        // 根据 status 自动设置对应的 statusDesc
        if (orderInfo.getStatus() != null) {
            String statusDesc = getStatusDescByStatus(orderInfo.getStatus());
            if (statusDesc != null) {
                orderInfo.setStatusDesc(statusDesc);
            }
        }
        
        return orderInfoMapper.updateOrderInfo(orderInfo);
    }
    
    /**
     * 根据状态码获取状态描述
     * 支持前端简化状态码(0,1,2,3,4,5)和后端标准状态码(10,20,30,40,50,60)
     */
    private String getStatusDescByStatus(Integer status) {
        if (status == null) return null;
        
        switch (status) {
            // 前端简化状态码
            case 0: return "待付款";
            case 1: return "待发货";
            case 2: return "待收货";
            case 3: return "待评价";
            case 4: return "交易完成";
            case 5: return "已取消";
            // 后端标准状态码
            case 10: return "待付款";
            case 20: return "待发货";
            case 30: return "待收货";
            case 40: return "待评价";
            case 50: return "交易完成";
            case 60: return "已取消";
            default: return null;
        }
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
