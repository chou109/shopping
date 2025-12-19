package com.neuedu.system.service.impl;
import lombok.RequiredArgsConstructor;
import java.util.List;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.neuedu.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.neuedu.system.mapper.OrderDetailMapper;
import com.neuedu.system.domain.OrderDetail;
import com.neuedu.system.service.IOrderDetailService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 订单明细Service业务层处理
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class OrderDetailServiceImpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements IOrderDetailService
{
    private final OrderDetailMapper orderDetailMapper;

    /**
     * 查询订单明细
     * 
     * @param id 订单明细主键
     * @return 订单明细
     */
    @Override
    public OrderDetail selectOrderDetailById(Long id)
    {
        return orderDetailMapper.selectOrderDetailById(id);
    }

    /**
     * 根据订单编号查询订单明细
     *
     * @param orderNo 订单编号
     * @return 订单明细列表
     */
    @Override
    public List<OrderDetail> selectOrderDetailByOrderNo(String orderNo)
    {
        return orderDetailMapper.selectOrderDetailByOrderNo(orderNo);
    }

    /**
     * 根据用户ID查询订单明细
     *
     * @param userId 用户ID
     * @return 订单明细列表
     */
    @Override
    public List<OrderDetail> selectOrderDetailByUserId(Long userId)
    {
        return orderDetailMapper.selectOrderDetailByUserId(userId);
    }

    /**
     * 批量插入订单明细
     *
     * @param orderDetails 订单明细列表
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertOrderDetail(List<OrderDetail> orderDetails)
    {
        if (orderDetails == null || orderDetails.isEmpty()) {
            return 0;
        }

        int count = 0;
        // 循环插入（如果需要更高效，可以使用真正的批量插入）
        for (OrderDetail orderDetail : orderDetails) {
            orderDetail.setCreateTime(DateUtils.getNowDate());
            count += orderDetailMapper.insertOrderDetail(orderDetail);
        }
        return count;
    }

    /**
     * 查询订单明细列表
     * 
     * @param orderDetail 订单明细
     * @return 订单明细
     */
    @Override
    public List<OrderDetail> selectOrderDetailList(OrderDetail orderDetail)
    {
        return orderDetailMapper.selectOrderDetailList(orderDetail);
    }

    /**
     * 新增订单明细
     * 
     * @param orderDetail 订单明细
     * @return 结果
     */
    @Override
    public int insertOrderDetail(OrderDetail orderDetail)
    {
        orderDetail.setCreateTime(DateUtils.getNowDate());
        return orderDetailMapper.insertOrderDetail(orderDetail);
    }

    /**
     * 修改订单明细
     * 
     * @param orderDetail 订单明细
     * @return 结果
     */
    @Override
    public int updateOrderDetail(OrderDetail orderDetail)
    {
        orderDetail.setUpdateTime(DateUtils.getNowDate());
        return orderDetailMapper.updateOrderDetail(orderDetail);
    }

    /**
     * 批量删除订单明细
     * 
     * @param ids 需要删除的订单明细主键
     * @return 结果
     */
    @Override
    public int deleteOrderDetailByIds(Long[] ids)
    {
        return orderDetailMapper.deleteOrderDetailByIds(ids);
    }

    /**
     * 删除订单明细信息
     * 
     * @param id 订单明细主键
     * @return 结果
     */
    @Override
    public int deleteOrderDetailById(Long id)
    {
        return orderDetailMapper.deleteOrderDetailById(id);
    }
}
