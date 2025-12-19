package com.neuedu.system.service;

import java.util.List;
import com.neuedu.system.domain.OrderStatusRecord;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 订单记录Service接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
public interface IOrderStatusRecordService extends IService<OrderStatusRecord>
{
    /**
     * 记录订单状态变更
     *
     * @param orderNo 订单编号
     * @param orderDetailNo 订单明细编号
     * @param productId 商品ID
     * @param productName 商品名称
     * @param status 状态
     * @param statusDesc 状态描述
     * @param remark 备注
     * @return 结果
     */
    int recordOrderStatus(String orderNo, String orderDetailNo, Long productId,
                          String productName, Integer status, String statusDesc, String remark);


    /**
     * 查询订单状态记录
     *
     * @param orderNo 订单编号
     * @return 状态记录列表
     */
    List<OrderStatusRecord> selectStatusRecordByOrderNo(String orderNo);

    /**
     * 查询订单明细状态记录
     *
     * @param orderDetailNo 订单明细编号
     * @return 状态记录列表
     */
    List<OrderStatusRecord> selectStatusRecordByOrderDetailNo(String orderDetailNo);



    /**
     * 查询订单记录
     * 
     * @param id 订单记录主键
     * @return 订单记录
     */
    public OrderStatusRecord selectOrderStatusRecordById(Long id);

    /**
     * 查询订单记录列表
     * 
     * @param orderStatusRecord 订单记录
     * @return 订单记录集合
     */
    public List<OrderStatusRecord> selectOrderStatusRecordList(OrderStatusRecord orderStatusRecord);

    /**
     * 新增订单记录
     * 
     * @param orderStatusRecord 订单记录
     * @return 结果
     */
    public int insertOrderStatusRecord(OrderStatusRecord orderStatusRecord);

    /**
     * 修改订单记录
     * 
     * @param orderStatusRecord 订单记录
     * @return 结果
     */
    public int updateOrderStatusRecord(OrderStatusRecord orderStatusRecord);

    /**
     * 批量删除订单记录
     * 
     * @param ids 需要删除的订单记录主键集合
     * @return 结果
     */
    public int deleteOrderStatusRecordByIds(Long[] ids);

    /**
     * 删除订单记录信息
     * 
     * @param id 订单记录主键
     * @return 结果
     */
    public int deleteOrderStatusRecordById(Long id);
}
