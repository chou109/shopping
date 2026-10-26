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
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        String response = assistant.executeTool("get_my_order_status", new ObjectMapper().readTree("{\"status\":20}"), current);
        assertEquals(Long.valueOf(42L), queriedUser.get());
        assertTrue(response.contains("****7654"));
        assertFalse(response.contains("ORDER-PRIVATE-987654"));

        GogoAssistantService.ToolResult result = assistant.executeToolResult("get_my_order_status",
                new ObjectMapper().readTree("{\"status\":20}"), current);
        assertEquals("get_my_order_status", result.display.get("tool"));
        assertFalse(result.display.toString().contains("ORDER-PRIVATE-987654"));
        assertTrue(result.display.toString().contains("****7654"));
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

    @Test
    public void orderToolRejectsIdentityOverridesAndNonNumericStatus() throws Exception {
        AtomicReference<Boolean> called = new AtomicReference<>(false);
        IOrderInfoService orders = proxy(IOrderInfoService.class, (proxy, method, args) -> {
            if ("selectOrderListByUserId".equals(method.getName())) called.set(true);
            return defaultValue(method.getReturnType());
        });
        GogoAssistantService assistant = new GogoAssistantService(new ObjectMapper(), proxy(FAQRepository.class, emptyProxy()),
                proxy(ProductRepository.class, emptyProxy()), orders, new GogoPromptFactory());
        LoginUser current = new LoginUser(); current.setUserId(42L);

        assertTrue(assistant.executeTool("get_my_order_status", new ObjectMapper().readTree("{\"userId\":999}"), current).contains("参数无效"));
        assertTrue(assistant.executeTool("get_my_order_status", new ObjectMapper().readTree("{\"status\":\"20\"}"), current).contains("状态条件无效"));
        assertFalse(called.get());
    }

    @Test
    public void productToolRequiresAStringAndRejectsAdditionalArguments() throws Exception {
        AtomicReference<Boolean> called = new AtomicReference<>(false);
        ProductRepository products = proxy(ProductRepository.class, (proxy, method, args) -> {
            if ("searchActiveByKeyword".equals(method.getName())) called.set(true);
            return Collections.emptyList();
        });
        GogoAssistantService assistant = new GogoAssistantService(new ObjectMapper(), proxy(FAQRepository.class, emptyProxy()),
                products, proxy(IOrderInfoService.class, emptyProxy()), new GogoPromptFactory());

        GogoAssistantService.ToolResult invalid = assistant.executeToolResult("search_products",
                new ObjectMapper().readTree("{\"query\":7}"), null);
        assertTrue(invalid.content.contains("搜索参数无效"));
        assertFalse(invalid.success);
        assertTrue(assistant.executeTool("search_products", new ObjectMapper().readTree("{\"query\":\"手机\",\"limit\":1000}"), null).contains("搜索参数无效"));
        assertFalse(called.get());
    }

    @Test
    public void productToolReturnsOnlySafeFieldsForTheResultCard() throws Exception {
        ProductRepository products = proxy(ProductRepository.class, (proxy, method, args) -> {
            if ("searchActiveByKeyword".equals(method.getName())) {
                com.neuedu.system.demoai.entity.Product product = new com.neuedu.system.demoai.entity.Product();
                product.setId(17L);
                product.setName("轻便耳机");
                product.setPrice(new BigDecimal("129.90"));
                product.setStock(8);
                product.setMainImage("private-storage-path.jpg");
                return Collections.singletonList(product);
            }
            return defaultValue(method.getReturnType());
        });
        GogoAssistantService assistant = new GogoAssistantService(new ObjectMapper(), proxy(FAQRepository.class, emptyProxy()),
                products, proxy(IOrderInfoService.class, emptyProxy()), new GogoPromptFactory());

        GogoAssistantService.ToolResult result = assistant.executeToolResult("search_products",
                new ObjectMapper().readTree("{\"query\":\"耳机\"}"), null);

        assertTrue(result.success);
        assertEquals("search_products", result.display.get("tool"));
        assertTrue(result.content.contains("商品ID 17"));
        assertTrue(result.display.toString().contains("129.90"));
        assertTrue(result.display.toString().contains("库存=8") || result.display.toString().contains("stock=8"));
        assertFalse(result.display.toString().contains("private-storage-path"));
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
