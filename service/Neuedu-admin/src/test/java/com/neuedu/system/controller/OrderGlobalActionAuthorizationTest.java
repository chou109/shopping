package com.neuedu.system.controller;

import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.service.IOrderInfoService;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class OrderGlobalActionAuthorizationTest {
    @After
    public void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void merchantCannotRunGlobalOrderActionsOrReadPlatformStatistics() {
        AtomicInteger serviceCalls = new AtomicInteger();
        OrderInfoAdminController controller = controller(serviceCalls);
        authenticateAs(71L);

        assertDenied(controller.adminCancelOrder("order-1", "取消原因"), "仅管理员可取消订单");
        assertDenied(controller.autoCancelExpiredOrders(), "仅管理员可批量取消超时订单");
        assertDenied(controller.autoConfirmReceipt(), "仅管理员可批量确认收货");
        assertDenied(controller.getStatistics(null, null), "仅管理员可查看全平台订单统计");
        assertEquals(0, serviceCalls.get());
    }

    @Test
    public void administratorCanRunGlobalOrderActionsAndReadPlatformStatistics() {
        AtomicInteger serviceCalls = new AtomicInteger();
        OrderInfoAdminController controller = controller(serviceCalls);
        authenticateAs(1L);

        assertEquals(200, code(controller.adminCancelOrder("order-1", "取消原因")));
        assertEquals(200, code(controller.autoCancelExpiredOrders()));
        assertEquals(200, code(controller.autoConfirmReceipt()));
        assertEquals(200, code(controller.getStatistics(null, null)));
        assertEquals(4, serviceCalls.get());
    }

    private void assertDenied(AjaxResult result, String expectedMessage) {
        assertEquals(expectedMessage, result.get("msg"));
    }

    private int code(AjaxResult result) {
        return ((Number) result.get("code")).intValue();
    }

    private void authenticateAs(Long userId) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        LoginUser loginUser = new LoginUser(userId, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    private OrderInfoAdminController controller(AtomicInteger serviceCalls) {
        InvocationHandler handler = (proxy, method, args) -> {
            switch (method.getName()) {
                case "cancelOrder":
                case "autoCancelExpiredOrders":
                case "autoConfirmReceipt":
                    serviceCalls.incrementAndGet();
                    return 1;
                case "getOrderStatistics":
                    serviceCalls.incrementAndGet();
                    return Collections.<String, Object>emptyMap();
                default:
                    return null;
            }
        };
        IOrderInfoService orderService = (IOrderInfoService) Proxy.newProxyInstance(
            IOrderInfoService.class.getClassLoader(), new Class<?>[]{IOrderInfoService.class}, handler);
        return new OrderInfoAdminController(orderService);
    }
}
