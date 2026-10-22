package com.neuedu.system.controller;

import com.neuedu.system.service.IOrderInfoService;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class OrderRefundAuthorizationTest {
    @Test
    public void merchantCannotProcessRefundEvenWithOrderEditPermission() {
        AtomicInteger refundCalls = new AtomicInteger();
        OrderInfoAdminController controller = controller(refundCalls);

        Object result = controller.processRefundForUser(71L, "refund-1", 3, "审核通过");

        assertEquals("仅管理员可处理退款", ((java.util.Map<?, ?>) result).get("msg"));
        assertEquals(0, refundCalls.get());
    }

    @Test
    public void administratorCanProcessRefund() {
        AtomicInteger refundCalls = new AtomicInteger();
        OrderInfoAdminController controller = controller(refundCalls);

        Object result = controller.processRefundForUser(1L, "refund-1", 3, "审核通过");

        assertEquals(200, ((Number) ((java.util.Map<?, ?>) result).get("code")).intValue());
        assertEquals(1, refundCalls.get());
    }

    private OrderInfoAdminController controller(AtomicInteger refundCalls) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("processRefund".equals(method.getName())) {
                refundCalls.incrementAndGet();
                return 1;
            }
            return null;
        };
        IOrderInfoService orderService = (IOrderInfoService) Proxy.newProxyInstance(
            IOrderInfoService.class.getClassLoader(), new Class<?>[]{IOrderInfoService.class}, handler);
        return new OrderInfoAdminController(orderService);
    }
}
