package com.neuedu.system.service;

import com.neuedu.system.mapper.CartMapper;
import com.neuedu.system.service.impl.CartServiceImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;

public class CartServiceOwnershipTest {
    @Test
    public void deleteByIdScopesUpdateToOwner() throws Exception {
        CartServiceImpl service = new CartServiceImpl(null);
        Long[] requested = new Long[2];
        CartMapper mapper = proxy(CartMapper.class, (method, args) -> {
            if ("deleteCartByIdForUser".equals(method)) {
                requested[0] = (Long) args[0];
                requested[1] = (Long) args[1];
                return 1;
            }
            return 0;
        });
        inject(service, mapper);

        assertEquals(1, service.deleteCartByIdForUser(15L, 71L));
        assertEquals(Long.valueOf(15L), requested[0]);
        assertEquals(Long.valueOf(71L), requested[1]);
    }

    @Test
    public void batchDeleteScopesUpdateToOwner() throws Exception {
        CartServiceImpl service = new CartServiceImpl(null);
        Long[] requestedUser = new Long[1];
        CartMapper mapper = proxy(CartMapper.class, (method, args) -> {
            if ("deleteCartByIdsForUser".equals(method)) {
                requestedUser[0] = (Long) args[1];
                return 2;
            }
            return 0;
        });
        inject(service, mapper);

        assertEquals(2, service.deleteCartByIdsForUser(new Long[]{15L, 16L}, 71L));
        assertEquals(Long.valueOf(71L), requestedUser[0]);
    }

    private static void inject(CartServiceImpl service, CartMapper mapper) throws Exception {
        Field field = CartServiceImpl.class.getDeclaredField("cartMapper");
        field.setAccessible(true);
        field.set(service, mapper);
    }

    private interface ProxyCall {
        Object invoke(String method, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
