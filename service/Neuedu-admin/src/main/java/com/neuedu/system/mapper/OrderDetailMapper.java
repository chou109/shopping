package com.neuedu.system.mapper;

import java.util.List;
import com.neuedu.system.domain.OrderDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单明细Mapper接口
 * 
 * @author ruoyi
 * @date 2025-12-13
 */
@Mapper
public interface OrderDetailMapper extends BaseMapper<OrderDetail>
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
     * 根据订单编号查询订单明细
     *
     * @param orderNo 订单编号
     * @return 订单明细列表
     */
    public List<OrderDetail> selectOrderDetailByOrderNo(String orderNo);

    /**
     * 根据用户ID查询订单明细
     *
     * @param userId 用户ID
     * @return 订单明细列表
     */
    public List<OrderDetail> selectOrderDetailByUserId(Long userId);


    /**
     * 新增订单明细
     * 
     * @param orderDetail 订单明细
     * @return 结果
     */
    public int insertOrderDetail(OrderDetail orderDetail);

    /**
     * 批量插入订单明细
     *
     * @param orderDetails 订单明细列表
     * @return 结果
     */
    public int batchInsertOrderDetail(List<OrderDetail> orderDetails);

    /**
     * 修改订单明细
     * 
     * @param orderDetail 订单明细
     * @return 结果
     */
    public int updateOrderDetail(OrderDetail orderDetail);

    /**
     * 更新订单明细状态
     *
     * @param orderDetailNo 订单明细编号
     * @param status 状态
     * @param statusDesc 状态描述
     * @return 结果
     */
    public int updateOrderDetailStatus(@Param("orderDetailNo") String orderDetailNo,
                                       @Param("status") Integer status,
                                       @Param("statusDesc") String statusDesc);

    /**
     * 删除订单明细
     * 
     * @param id 订单明细主键
     * @return 结果
     */
    public int deleteOrderDetailById(Long id);

    /**
     * 批量删除订单明细
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrderDetailByIds(Long[] ids);
}
