package com.neuedu.system.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.neuedu.system.service.IOrderInfoService;

import lombok.extern.slf4j.Slf4j;

/**
 * 订单定时任务
 */
@Component
@Slf4j
public class OrderJob {

    @Autowired
    private IOrderInfoService orderInfoService;

    /**
     * 每分钟执行一次，检查超时订单
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void autoCancelExpiredOrders() {
        try {
            int count = orderInfoService.autoCancelExpiredOrders();
            if (count > 0) {
                log.info("自动取消超时订单完成，共取消 {} 个订单", count);
            }
        } catch (Exception e) {
            log.error("自动取消超时订单失败", e);
        }
    }

    /**
     * 每天凌晨2点执行，自动确认收货
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoConfirmReceipt() {
        try {
            int count = orderInfoService.autoConfirmReceipt();
            if (count > 0) {
                log.info("自动确认收货完成，共确认 {} 个订单", count);
            }
        } catch (Exception e) {
            log.error("自动确认收货失败", e);
        }
    }
}