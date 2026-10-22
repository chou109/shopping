package com.neuedu.system.controller;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.service.IOrderInfoService;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class OrderDeletionStatusTest {
    private static final Long USER_ID = 71L;
    private static final Long ORDER_ID = 91L;

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void rejectsDeletingOrdersThatAreWaitingForShipmentReceiptOrReview() {
        for (int status : new int[]{20, 30, 40}) {
            AtomicInteger conditionalDeleteCalls = new AtomicInteger();
            OrderInfoController controller = controller(status, conditionalDeleteCalls);

            Map<?, ?> result = (Map<?, ?>) controller.userDelete(ORDER_ID);

            assertEquals(500, ((Number) result.get("code")).intValue());
            assertEquals("仅待付款、已完成或已取消的订单可以删除", result.get("msg"));
            assertEquals(0, conditionalDeleteCalls.get());
        }
    }

    @Test
    public void deletesAnAllowedOrderUsingAnAtomicOwnerAndStatusScopedUpdate() {
        for (int status : new int[]{10, 50, 60}) {
            AtomicInteger conditionalDeleteCalls = new AtomicInteger();
            Long[] deleteArgs = new Long[2];
            OrderInfoController controller = controller(status, (id, userId) -> {
                deleteArgs[0] = id;
                deleteArgs[1] = userId;
                conditionalDeleteCalls.incrementAndGet();
                return 1;
            });

            Map<?, ?> result = (Map<?, ?>) controller.userDelete(ORDER_ID);

            assertEquals(200, ((Number) result.get("code")).intValue());
            assertEquals(1, conditionalDeleteCalls.get());
            assertEquals(ORDER_ID, deleteArgs[0]);
            assertEquals(USER_ID, deleteArgs[1]);
        }
    }

    @Test
    public void doesNotReportSuccessWhenOrderStatusChangesBeforeDeletion() {
        OrderInfoController controller = controller(10, (id, userId) -> 0);

        Map<?, ?> result = (Map<?, ?>) controller.userDelete(ORDER_ID);

        assertEquals(500, ((Number) result.get("code")).intValue());
        assertEquals("订单状态已变更或订单已删除，请刷新后重试", result.get("msg"));
    }

    private OrderInfoController controller(int status, AtomicInteger deleteCalls) {
        return controller(status, (id, userId) -> {
            deleteCalls.incrementAndGet();
            return 1;
        });
    }

    private OrderInfoController controller(int status, DeleteCall deleteCall) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("selectOrderInfoById".equals(method.getName())) {
                OrderInfo order = new OrderInfo();
                order.setId(ORDER_ID);
                order.setUserId(USER_ID);
                order.setStatus(status);
                return order;
            }
            if ("deleteMyOrderForUser".equals(method.getName())) {
                return deleteCall.invoke((Long) args[0], (Long) args[1]);
            }
            return null;
        };
        IOrderInfoService service = (IOrderInfoService) Proxy.newProxyInstance(
            IOrderInfoService.class.getClassLoader(), new Class<?>[]{IOrderInfoService.class}, handler);

        SysUser user = new SysUser();
        user.setUserId(USER_ID);
        LoginUser loginUser = new LoginUser(USER_ID, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
        return new OrderInfoController(service);
    }

    private interface DeleteCall {
        int invoke(Long id, Long userId);
    }
}
