package com.neuedu.system.controller;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.Product;
import com.neuedu.system.service.IOrderInfoService;
import com.neuedu.system.service.IProductService;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class OrderInfoDirectOrderValidationTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void rejectsMissingAndMalformedDirectOrderFieldsWithoutCallingService() throws Exception {
        Fixture fixture = new Fixture();

        assertEquals("订单参数不能为空", fixture.message(fixture.controller.createDirectOrder(null)));
        assertEquals("商品 ID必须为正整数", fixture.message(fixture.controller.createDirectOrder(new HashMap<>())));

        Map<String, Object> badQuantity = params();
        badQuantity.put("quantity", "many");
        assertEquals("购买数量必须为正整数", fixture.message(fixture.controller.createDirectOrder(badQuantity)));

        Map<String, Object> badAddress = params();
        badAddress.put("addressId", 0);
        assertEquals("收货地址 ID必须为正整数", fixture.message(fixture.controller.createDirectOrder(badAddress)));

        Map<String, Object> badBlindBoxFlag = params();
        badBlindBoxFlag.put("isBlindBox", "yes");
        assertEquals("盲盒参数格式无效", fixture.message(fixture.controller.createDirectOrder(badBlindBoxFlag)));
        assertEquals(0, fixture.serviceCalls.get());
    }

    @Test
    public void createsDirectOrderAfterValidatingPositiveIdsAndQuantity() throws Exception {
        Fixture fixture = new Fixture();

        assertNotNull(fixture.controller.createDirectOrder(params()));

        assertEquals(1, fixture.serviceCalls.get());
    }

    private static Map<String, Object> params() {
        Map<String, Object> params = new HashMap<>();
        params.put("productId", 101);
        params.put("quantity", 2);
        params.put("addressId", 9);
        return params;
    }

    private static class Fixture {
        private final AtomicInteger serviceCalls = new AtomicInteger();
        private final OrderInfoController controller;

        private Fixture() throws Exception {
            SysUser user = new SysUser();
            user.setUserId(7L);
            LoginUser loginUser = new LoginUser(7L, 1L, user, Collections.emptySet());
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

            IOrderInfoService orderService = proxy(IOrderInfoService.class, (method, args) -> {
                if ("createDirectOrder".equals(method)) {
                    serviceCalls.incrementAndGet();
                    return new OrderInfo();
                }
                return null;
            });
            controller = new OrderInfoController(orderService);
            Product product = new Product();
            product.setIsBlindBox(0);
            inject("productService", proxy(IProductService.class, (method, args) ->
                "selectProductById".equals(method) ? product : null));
        }

        private String message(Object result) {
            return (String) ((Map<?, ?>) result).get("msg");
        }

        private void inject(String fieldName, Object value) throws Exception {
            Field field = OrderInfoController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
        }
    }

    private interface ProxyCall {
        Object invoke(String methodName, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
