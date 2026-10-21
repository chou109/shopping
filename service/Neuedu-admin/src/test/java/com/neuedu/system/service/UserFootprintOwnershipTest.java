package com.neuedu.system.service;

import com.neuedu.system.mapper.UserFootprintMapper;
import com.neuedu.system.service.impl.UserFootprintServiceImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;

public class UserFootprintOwnershipTest {
    @Test
    public void deleteByIdIncludesOwnerInMapperUpdate() throws Exception {
        UserFootprintServiceImpl service = new UserFootprintServiceImpl(null, null, null, null);
        Long[] requested = new Long[2];
        UserFootprintMapper mapper = proxy(UserFootprintMapper.class, (method, args) -> {
            if ("deleteUserFootprintByIdForUser".equals(method)) {
                requested[0] = (Long) args[0];
                requested[1] = (Long) args[1];
                return 1;
            }
            return 0;
        });
        Field field = UserFootprintServiceImpl.class.getDeclaredField("userFootprintMapper");
        field.setAccessible(true);
        field.set(service, mapper);

        assertEquals(1, service.deleteUserFootprintByIdForUser(28L, 71L));
        assertEquals(Long.valueOf(28L), requested[0]);
        assertEquals(Long.valueOf(71L), requested[1]);
    }

    private interface ProxyCall {
        Object invoke(String method, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
