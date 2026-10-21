package com.neuedu.system.controller;

import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IShopService;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ShopPublicSearchControllerTest {
    @Test
    public void publicSearchAlwaysFiltersToActiveShops() {
        final String[] queriedKeyword = new String[1];
        final Integer[] queriedStatus = new Integer[1];
        IShopService service = proxy(IShopService.class, (methodName, args) -> {
            if ("searchShops".equals(methodName)) {
                queriedKeyword[0] = (String) args[0];
                queriedStatus[0] = (Integer) args[1];
                return Collections.<Shop>emptyList();
            }
            return null;
        });

        AjaxResult result = new ShopController(service).search("coffee");

        assertEquals("coffee", queriedKeyword[0]);
        assertEquals(Integer.valueOf(1), queriedStatus[0]);
        assertEquals(200, result.get("code"));
    }

    @Test
    public void publicDetailsRejectInactiveShops() {
        Shop shop = new Shop();
        shop.setStatus(2);
        IShopService service = proxy(IShopService.class, (methodName, args) ->
            "selectShopById".equals(methodName) ? shop : null);

        AjaxResult result = new ShopController(service).getPublicInfo(4L);

        assertNotEquals(200, result.get("code"));
    }

    private interface ProxyCall {
        Object invoke(String methodName, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
