package com.neuedu.system.controller;

import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IShopService;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ShopControllerOwnershipTest {
    @Test
    public void ownerCanManageTheirShopOnly() {
        Shop ownShop = new Shop();
        ownShop.setId(12L);
        ShopController controller = controllerFor(ownShop);

        assertTrue(controller.canManageShop(12L, 71L));
        assertFalse(controller.canManageShop(13L, 71L));
    }

    @Test
    public void administratorCanManageAnyShop() {
        ShopController controller = controllerFor(null);

        assertTrue(controller.canManageShop(99L, 1L));
    }

    private ShopController controllerFor(Shop ownShop) {
        IShopService service = proxy(IShopService.class, (method, args) ->
            "selectShopByUserId".equals(method) ? ownShop : null);
        return new ShopController(service);
    }

    private interface ProxyCall {
        Object invoke(String method, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
