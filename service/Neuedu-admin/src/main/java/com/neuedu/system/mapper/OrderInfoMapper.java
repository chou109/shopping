package com.neuedu.system.mapper;

import java.util.List;
import java.util.Map;

import com.neuedu.system.domain.OrderInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Mapper
public interface OrderInfoMapper extends BaseMapper<OrderInfo>
{
    /**
     * 查询订单
     * 
     * @param id 订单主键
     * @return 订单
     */
    public OrderInfo selectOrderInfoById(Long id);

    /** 锁定当前用户包含指定商品且未删除的订单，供评价幂等校验使用。 */
    public OrderInfo lockOrderForProductReview(@Param("userId") Long userId,
                                               @Param("orderId") Long orderId,
                                               @Param("productId") Long productId);

    /** 锁定当前用户订单，防止评价与完成状态并发穿透。 */
    public OrderInfo lockOrderForCompletion(@Param("userId") Long userId,
                                            @Param("orderNo") String orderNo);

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
    public List<OrderInfo> selectOrderListByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询超时未支付订单
     *
     * @return 订单列表
     */
    public List<OrderInfo> selectExpiredOrders();

    /**
     * 查询待确认收货订单
     *
     * @return 订单列表
     */
    public List<OrderInfo> selectWaitConfirmOrders();




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
     * 更新订单状态
     *
     * @param orderNo 订单编号
     * @param status 状态
     * @param statusDesc 状态描述
     * @return 结果
     */
    public int updateOrderStatus(@Param("orderNo") String orderNo, @Param("status") Integer status, @Param("statusDesc") String statusDesc);

    /**
     * 仅在订单仍处于待发货时原子取消，防止并发退款重复入账
     */
    public int cancelOrderForRefund(@Param("orderNo") String orderNo,
                                    @Param("reason") String reason,
                                    @Param("expectedStatus") Integer expectedStatus,
                                    @Param("cancelledStatus") Integer cancelledStatus,
                                    @Param("statusDesc") String statusDesc);

    /**
     * 取消订单
     *
     * @param orderNo 订单编号
     * @param cancelReason 取消原因
     * @return 结果
     */
    public int cancelOrder(@Param("orderNo") String orderNo, @Param("cancelReason") String cancelReason);

    /**
     * 支付订单
     *
     * @param orderNo 订单编号
     * @param paymentType 支付类型
     * @param paymentTypeDesc 支付类型描述
     * @return 结果
     */
    public int payOrder(@Param("orderNo") String orderNo, @Param("paymentType") Integer paymentType, @Param("paymentTypeDesc") String paymentTypeDesc);

    /**
     * 删除订单
     * 
     * @param id 订单主键
     * @return 结果
     */
    public int deleteOrderInfoById(Long id);

    /** Soft-delete a consumer order only while it belongs to the user and is in a deletable state. */
    public int deleteMyOrderForUser(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 批量删除订单
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrderInfoByIds(Long[] ids);

    /**
     * 订单统计查询
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param userId 用户ID
     * @return 统计信息
     */
    public Map<String, Object> selectOrderStatistics(@Param("startTime") String startTime,
                                                     @Param("endTime") String endTime,
                                                     @Param("userId") Long userId);

    /**
     * 每日订单统计
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 每日统计列表
     */
    public List<Map<String, Object>> selectDailyOrderStatistics(@Param("startTime") String startTime,
                                                                @Param("endTime") String endTime,
                                                                @Param("userId") Long userId);
}
