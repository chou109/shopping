package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.OrderStatusRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
/**
 * 订单记录Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Mapper
public interface OrderStatusRecordMapper extends BaseMapper<OrderStatusRecord>
{
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
     * 删除订单记录
     * 
     * @param id 订单记录主键
     * @return 结果
     */
    public int deleteOrderStatusRecordById(Long id);

    /**
     * 批量删除订单记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrderStatusRecordByIds(Long[] ids);
}
