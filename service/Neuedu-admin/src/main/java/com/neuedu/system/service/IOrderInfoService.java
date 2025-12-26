package com.neuedu.system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import com.neuedu.system.domain.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 订单Service接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
public interface IOrderInfoService extends IService<OrderInfo>
{
    /**
     * 查询订单
     * 
     * @param id 订单主键
     * @return 订单
     */
    public OrderInfo selectOrderInfoById(Long id);

    /**
     * 查询订单列表
     * 
     * @param orderInfo 订单
     * @return 订单集合
     */
    public List<OrderInfo> selectOrderInfoList(OrderInfo orderInfo);

    /**
     * 根据订单编号查询订单
     *
     * @param orderNo 订单编号
     * @return 订单
     */
    public OrderInfo selectOrderInfoByOrderNo(String orderNo);


    /**
     * 查询用户订单列表
     *
     * @param userId 用户ID
     * @param status 订单状态
     * @return 订单列表
     */
    public List<OrderInfo> selectOrderListByUserId(Long userId, Integer status);

    /**
     * 创建订单（从购物车）
     *
     * @param userId 用户ID
     * @param addressId 地址ID
     * @return 订单信息
     */
    public OrderInfo createOrderFromCart(Long userId, Long addressId);

    /**
     * 直接购买创建订单（单个商品）
     *
     * @param userId 用户ID
     * @param productId 商品ID
     * @param quantity 数量
     * @param addressId 地址ID
     * @return 订单信息
     */
    public OrderInfo createDirectOrder(Long userId, Long productId, Integer quantity, Long addressId);

    /**
     * 支付订单
     *
     * @param orderNo 订单编号
     * @param paymentType 支付类型
     * @return 结果
     */
    public int payOrder(String orderNo, Integer paymentType);

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @param cancelReason 取消原因
     * @return 结果
     */
    public int cancelOrder(String orderNo, String cancelReason);


    /**
     * 发货
     *
     * @param orderNo 订单编号
     * @param shippingInfo 物流信息
     * @return 结果
     */
    public int shipOrder(String orderNo, Map<String, String> shippingInfo);

    /**
     * 确认收货
     *
     * @param orderNo 订单编号
     * @return 结果
     */
    public int confirmReceipt(String orderNo);

    /**
     * 申请退款
     *
     * @param orderNo 订单编号
     * @param reason 退款原因
     * @param description 退款说明
     * @param contactPhone 联系电话
     * @return 结果
     */
    public int applyRefund(String orderNo, String reason, String description, String contactPhone);

    /**
     * 处理退款
     *
     * @param refundNo 退款单号
     * @param refundStatus 退款状态
     * @param auditOpinion 审核意见
     * @return 结果
     */
    public int processRefund(String refundNo, Integer refundStatus, String auditOpinion);


    /**
     * 获取订单统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    public Map<String, Object> getOrderStatistics(String startTime, String endTime);

    /**
     * 自动取消超时订单
     *
     * @return 取消的订单数量
     */
    public int autoCancelExpiredOrders();


    /**
     * 自动确认收货
     *
     * @return 确认的订单数量
     */
    public int autoConfirmReceipt();




    /**
     * 新增订单
     * 
     * @param orderInfo 订单
     * @return 结果
     */
    public int insertOrderInfo(OrderInfo orderInfo);

    /**
     * 修改订单
     * 
     * @param orderInfo 订单
     * @return 结果
     */
    public int updateOrderInfo(OrderInfo orderInfo);

    /**
     * 批量删除订单
     * 
     * @param ids 需要删除的订单主键集合
     * @return 结果
     */
    public int deleteOrderInfoByIds(Long[] ids);

    /**
     * 删除订单信息
     * 
     * @param id 订单主键
     * @return 结果
     */
    public int deleteOrderInfoById(Long id);
}
