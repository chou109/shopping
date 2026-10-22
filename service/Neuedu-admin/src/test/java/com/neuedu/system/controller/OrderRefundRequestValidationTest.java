package com.neuedu.system.controller;

import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.service.IOrderInfoService;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class OrderRefundRequestValidationTest {
    @Test
    public void missingOrMalformedRequestIsRejectedBeforeOrderLookup() {
        AtomicInteger lookups = new AtomicInteger();
        OrderInfoController controller = controller(lookups, new AtomicInteger(), new AtomicReference<>());

        assertEquals("订单编号不能为空", message(controller.applyRefundForUser(42L, null)));
        assertEquals("订单编号不能为空", message(controller.applyRefundForUser(42L,
                Collections.singletonMap("orderNo", 123))));
        assertEquals(0, lookups.get());
    }

    @Test
    public void refundReasonMustBePresentAndWithinDatabaseLimit() {
        AtomicInteger lookups = new AtomicInteger();
        OrderInfoController controller = controller(lookups, new AtomicInteger(), new AtomicReference<>());
        Map<String, Object> request = request(" ");

        assertEquals("请选择取消原因", message(controller.applyRefundForUser(42L, request)));
        request.put("reason", String.join("", Collections.nCopies(501, "a")));
        assertEquals("取消原因不能超过500个字符", message(controller.applyRefundForUser(42L, request)));
    }

    @Test
    public void refundRequestRequiresOrderOwnership() {
        AtomicInteger refundCalls = new AtomicInteger();
        OrderInfoController controller = controller(new AtomicInteger(), refundCalls, new AtomicReference<>());

        assertEquals("无权操作此订单", message(controller.applyRefundForUser(7L, request("不想要了"))));
        assertEquals(0, refundCalls.get());
    }

    @Test
    public void validRefundRequestTrimsUserInputAndCallsServiceOnce() {
        AtomicInteger refundCalls = new AtomicInteger();
        AtomicReference<String> receivedReason = new AtomicReference<>();
        OrderInfoController controller = controller(new AtomicInteger(), refundCalls, receivedReason);

        Map<String, Object> request = request("  不想要了  ");
        request.put("orderNo", "  order-42  ");

        assertEquals(200, ((Number) controller.applyRefundForUser(42L, request).get("code")).intValue());
        assertEquals("不想要了", receivedReason.get());
        assertEquals(1, refundCalls.get());
    }

    private OrderInfoController controller(AtomicInteger lookups, AtomicInteger refundCalls,
                                           AtomicReference<String> receivedReason) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("selectOrderInfoByOrderNo".equals(method.getName())) {
                lookups.incrementAndGet();
                OrderInfo order = new OrderInfo();
                order.setUserId(42L);
                return order;
            }
            if ("applyRefund".equals(method.getName())) {
                refundCalls.incrementAndGet();
                receivedReason.set((String) args[1]);
                return 1;
            }
            return null;
        };
        IOrderInfoService service = (IOrderInfoService) Proxy.newProxyInstance(
                IOrderInfoService.class.getClassLoader(), new Class<?>[]{IOrderInfoService.class}, handler);
        return new OrderInfoController(service);
    }

    private Map<String, Object> request(String reason) {
        Map<String, Object> request = new HashMap<>();
        request.put("orderNo", "order-42");
        request.put("reason", reason);
        return request;
    }

    private String message(Map<?, ?> result) {
        return (String) result.get("msg");
    }
}
