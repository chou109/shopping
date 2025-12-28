package com.neuedu.system.demoai.service;

import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.service.IOrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 订单服务 - 用于AI客服查询订单信息
 */
@Slf4j
@Service
public class OrderService {

    @Autowired
    private IOrderInfoService orderInfoService;

    /**
     * 根据订单号查询订单
     */
    public OrderInfo getOrderByOrderNo(String orderNo) {
        try {
            return orderInfoService.selectOrderInfoByOrderNo(orderNo);
        } catch (Exception e) {
            log.error("查询订单失败, orderNo={}", orderNo, e);
            return null;
        }
    }

    /**
     * 查询用户订单列表
     * @param userId 用户ID
     * @param status 订单状态（可选）
     */
    public List<OrderInfo> getOrdersByUserId(Long userId, Integer status) {
        try {
            return orderInfoService.selectOrderListByUserId(userId, status);
        } catch (Exception e) {
            log.error("查询用户订单失败, userId={}, status={}", userId, status, e);
            return null;
        }
    }

    /**
     * 根据订单号获取订单状态描述
     */
    public String getOrderStatusDesc(String orderNo) {
        OrderInfo orderInfo = getOrderByOrderNo(orderNo);
        if (orderInfo != null) {
            return orderInfo.getStatusDesc();
        }
        return null;
    }

    /**
     * 检查订单是否存在
     */
    public boolean orderExists(String orderNo) {
        return getOrderByOrderNo(orderNo) != null;
    }
}
