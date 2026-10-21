package com.neuedu.system.controller;

import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.OrderDetail;
import com.neuedu.system.domain.Product;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IOrderInfoService;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.IShopService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
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

    @Test
    public void merchantCanViewOnlyOrdersWhoseItemsBelongToTheirShop() throws Exception {
        Shop ownedShop = new Shop();
        ownedShop.setId(12L);
        OrderInfo order = orderWithProduct(44L);

        assertTrue(controllerFor(ownedShop, new AtomicInteger(), 12L).canViewOrder(order, 71L));
        assertFalse(controllerFor(ownedShop, new AtomicInteger(), 99L).canViewOrder(order, 71L));
        assertFalse(controllerFor(ownedShop, new AtomicInteger(), null).canViewOrder(order, 71L));
        assertTrue(controllerFor(null, new AtomicInteger(), null).canViewOrder(order, 1L));
    }

    @Test
    public void onlyAdministratorIdentityCanUseGenericOrderManagement() throws Exception {
        OrderInfoAdminController controller = controllerFor(null, new AtomicInteger());

        assertTrue(controller.isAdministrator(1L));
        assertFalse(controller.isAdministrator(71L));
        assertFalse(controller.isAdministrator(null));
    }

    private OrderInfo orderWithProduct(Long productId) {
        OrderDetail detail = new OrderDetail();
        detail.setProductId(productId);
        OrderInfo order = new OrderInfo();
        order.setOrderDetails(Collections.singletonList(detail));
        return order;
    }

    private OrderInfoAdminController controllerFor(Shop shop, AtomicInteger shopLookups) throws Exception {
        return controllerFor(shop, shopLookups, null);
    }

    private OrderInfoAdminController controllerFor(Shop shop, AtomicInteger shopLookups, Long productShopId) throws Exception {
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
        IProductService productService = proxy(IProductService.class, (method, args) -> {
            if ("selectProductById".equals(method) && productShopId != null) {
                Product product = new Product();
                product.setShopId(productShopId);
                return product;
            }
            return null;
        });
        Field productField = OrderInfoAdminController.class.getDeclaredField("productService");
        productField.setAccessible(true);
        productField.set(controller, productService);
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
