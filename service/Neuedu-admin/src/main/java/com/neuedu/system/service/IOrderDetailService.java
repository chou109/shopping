package com.neuedu.system.service;

import java.util.List;
import com.neuedu.system.domain.OrderDetail;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 订单明细Service接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
public interface IOrderDetailService extends IService<OrderDetail>
{
    /**
     * 查询订单明细
     * 
     * @param id 订单明细主键
     * @return 订单明细
     */
    public OrderDetail selectOrderDetailById(Long id);

    /**
     * 查询订单明细列表
     * 
     * @param orderDetail 订单明细
     * @return 订单明细集合
     */
    public List<OrderDetail> selectOrderDetailList(OrderDetail orderDetail);

    /**
     * 新增订单明细
     * 
     * @param orderDetail 订单明细
     * @return 结果
     */
    public int insertOrderDetail(OrderDetail orderDetail);

    /**
     * 修改订单明细
     * 
     * @param orderDetail 订单明细
     * @return 结果
     */
    public int updateOrderDetail(OrderDetail orderDetail);

    /**
     * 批量删除订单明细
     * 
     * @param ids 需要删除的订单明细主键集合
     * @return 结果
     */
    public int deleteOrderDetailByIds(Long[] ids);

    /**
     * 删除订单明细信息
     * 
     * @param id 订单明细主键
     * @return 结果
     */
    public int deleteOrderDetailById(Long id);
}
