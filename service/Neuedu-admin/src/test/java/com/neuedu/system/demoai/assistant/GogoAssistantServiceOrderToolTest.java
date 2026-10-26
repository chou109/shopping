package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.repository.ProductRepository;
import com.neuedu.system.service.IOrderInfoService;
import com.neuedu.system.demoai.assistant.prompt.GogoPromptFactory;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class GogoAssistantServiceOrderToolTest {
    @Test
    public void orderToolAlwaysUsesAuthenticatedUserAndMasksOrderNumber() throws Exception {
        AtomicReference<Long> queriedUser = new AtomicReference<>();
        IOrderInfoService orders = proxy(IOrderInfoService.class, (proxy, method, args) -> {
            if ("selectOrderListByUserId".equals(method.getName())) {
                queriedUser.set((Long) args[0]);
                OrderInfo order = new OrderInfo(); order.setOrderNo("ORDER-PRIVATE-987654"); order.setStatusDesc("待发货");
                return Collections.singletonList(order);
            }
            return defaultValue(method.getReturnType());
        });
        GogoAssistantService assistant = new GogoAssistantService(new ObjectMapper(), proxy(FAQRepository.class, emptyProxy()),
                proxy(ProductRepository.class, emptyProxy()), orders, new GogoPromptFactory());
        LoginUser current = new LoginUser(); current.setUserId(42L);
        String response = assistant.executeTool("get_my_order_status", new ObjectMapper().readTree("{\"userId\":999,\"orderNo\":\"ORDER-PRIVATE-987654\"}"), current);
        assertEquals(Long.valueOf(42L), queriedUser.get());
        assertTrue(response.contains("****7654"));
        assertFalse(response.contains("ORDER-PRIVATE-987654"));
    }

    @Test
    public void orderToolRefusesUnauthenticatedCallsWithoutQueryingOrders() throws Exception {
        AtomicReference<Boolean> called = new AtomicReference<>(false);
        IOrderInfoService orders = proxy(IOrderInfoService.class, (proxy, method, args) -> {
            if ("selectOrderListByUserId".equals(method.getName())) called.set(true);
            return defaultValue(method.getReturnType());
        });
        GogoAssistantService assistant = new GogoAssistantService(new ObjectMapper(), proxy(FAQRepository.class, emptyProxy()),
                proxy(ProductRepository.class, emptyProxy()), orders, new GogoPromptFactory());
        String response = assistant.executeTool("get_my_order_status", new ObjectMapper().readTree("{}"), null);
        assertTrue(response.contains("请登录"));
        assertFalse(called.get());
    }

    private static InvocationHandler emptyProxy() { return (proxy, method, args) -> defaultValue(method.getReturnType()); }
    @SuppressWarnings("unchecked") private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }
    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0D;
        return null;
    }
}
