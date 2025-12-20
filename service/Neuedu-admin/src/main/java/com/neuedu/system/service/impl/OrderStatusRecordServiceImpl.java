package com.neuedu.system.service.impl;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.OrderStatusRecordMapper;
import com.neuedu.system.domain.OrderStatusRecord;
import com.neuedu.system.service.IOrderStatusRecordService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单记录Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderStatusRecordServiceImpl extends ServiceImpl<OrderStatusRecordMapper, OrderStatusRecord> implements IOrderStatusRecordService
{
    private final OrderStatusRecordMapper orderStatusRecordMapper;

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int recordOrderStatus(String orderNo, String orderDetailNo, Long productId,
                                 String productName, Integer status, String statusDesc, String remark)
    {
        OrderStatusRecord record = new OrderStatusRecord();
        record.setOrderNo(orderNo);
        record.setOrderDetailNo(orderDetailNo);
        record.setProductId(productId);
        record.setProductName(productName);
        record.setStatus(status);
        record.setStatusDesc(statusDesc);
        record.setRemark(remark);
        record.setCreateTime(DateUtils.getNowDate());

        return orderStatusRecordMapper.insertOrderStatusRecord(record);
    }

    /**
     * 查询订单状态记录
     *
     * @param orderNo 订单编号
     * @return 状态记录列表
     */
    @Override
    public List<OrderStatusRecord> selectStatusRecordByOrderNo(String orderNo)
    {
        OrderStatusRecord record = new OrderStatusRecord();
        record.setOrderNo(orderNo);
        return orderStatusRecordMapper.selectOrderStatusRecordList(record);
    }

    /**
     * 查询订单明细状态记录
     *
     * @param orderDetailNo 订单明细编号
     * @return 状态记录列表
     */
    @Override
    public List<OrderStatusRecord> selectStatusRecordByOrderDetailNo(String orderDetailNo)
    {
        OrderStatusRecord record = new OrderStatusRecord();
        record.setOrderDetailNo(orderDetailNo);
        return orderStatusRecordMapper.selectOrderStatusRecordList(record);
    }

    /**
     * 查询订单记录
     * 
     * @param id 订单记录主键
     * @return 订单记录
     */
    @Override
    public OrderStatusRecord selectOrderStatusRecordById(Long id)
    {
        return orderStatusRecordMapper.selectOrderStatusRecordById(id);
    }

    /**
     * 查询订单记录列表
     * 
     * @param orderStatusRecord 订单记录
     * @return 订单记录
     */
    @Override
    public List<OrderStatusRecord> selectOrderStatusRecordList(OrderStatusRecord orderStatusRecord)
    {
        return orderStatusRecordMapper.selectOrderStatusRecordList(orderStatusRecord);
    }

    /**
     * 新增订单记录
     * 
     * @param orderStatusRecord 订单记录
     * @return 结果
     */
    @Override
    public int insertOrderStatusRecord(OrderStatusRecord orderStatusRecord)
    {
        orderStatusRecord.setCreateTime(DateUtils.getNowDate());
        return orderStatusRecordMapper.insertOrderStatusRecord(orderStatusRecord);
    }

    /**
     * 修改订单记录
     * 
     * @param orderStatusRecord 订单记录
     * @return 结果
     */
    @Override
    public int updateOrderStatusRecord(OrderStatusRecord orderStatusRecord)
    {
        return orderStatusRecordMapper.updateOrderStatusRecord(orderStatusRecord);
    }

    /**
     * 批量删除订单记录
     * 
     * @param ids 需要删除的订单记录主键
     * @return 结果
     */
    @Override
    public int deleteOrderStatusRecordByIds(Long[] ids)
    {
        return orderStatusRecordMapper.deleteOrderStatusRecordByIds(ids);
    }

    /**
     * 删除订单记录信息
     * 
     * @param id 订单记录主键
     * @return 结果
     */
    @Override
    public int deleteOrderStatusRecordById(Long id)
    {
        return orderStatusRecordMapper.deleteOrderStatusRecordById(id);
    }
}
