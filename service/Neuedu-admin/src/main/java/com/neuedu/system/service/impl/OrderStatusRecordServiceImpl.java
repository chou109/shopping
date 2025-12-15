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
