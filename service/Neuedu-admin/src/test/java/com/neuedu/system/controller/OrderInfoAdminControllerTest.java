package com.neuedu.system.controller;

import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IOrderInfoService;
import com.neuedu.system.service.IShopService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OrderInfoAdminControllerTest {
    @Test
    public void merchantScopeOverridesClientShopIdWithOwnedShop() throws Exception {
        Shop ownedShop = new Shop();
        ownedShop.setId(12L);
        OrderInfoAdminController controller = controllerFor(ownedShop, new AtomicInteger());
        OrderInfo filter = new OrderInfo();
        filter.setShopId(999L);

        assertTrue(controller.applyShopScope(filter, 71L));
        assertEquals(Long.valueOf(12L), filter.getShopId());
    }

    @Test
    public void merchantWithoutShopCannotQueryOrders() throws Exception {
        OrderInfoAdminController controller = controllerFor(null, new AtomicInteger());
        OrderInfo filter = new OrderInfo();

        assertFalse(controller.applyShopScope(filter, 71L));
    }

    @Test
    public void administratorSeesAllShopsAndDoesNotResolveMerchantShop() throws Exception {
        AtomicInteger shopLookups = new AtomicInteger();
        OrderInfoAdminController controller = controllerFor(null, shopLookups);
        OrderInfo filter = new OrderInfo();
        filter.setShopId(999L);

        assertTrue(controller.applyShopScope(filter, 1L));
        assertNull(filter.getShopId());
        assertEquals(0, shopLookups.get());
    }

    private OrderInfoAdminController controllerFor(Shop shop, AtomicInteger shopLookups) throws Exception {
        IOrderInfoService orderService = proxy(IOrderInfoService.class, (method, args) -> null);
        IShopService shopService = proxy(IShopService.class, (method, args) -> {
            if ("selectShopByUserId".equals(method)) {
                shopLookups.incrementAndGet();
                return shop;
            }
            return null;
        });
        OrderInfoAdminController controller = new OrderInfoAdminController(orderService);
        Field field = OrderInfoAdminController.class.getDeclaredField("shopService");
        field.setAccessible(true);
        field.set(controller, shopService);
        return controller;
    }

    private interface ProxyCall {
        Object invoke(String method, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
