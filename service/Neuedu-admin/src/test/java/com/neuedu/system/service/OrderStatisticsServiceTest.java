package com.neuedu.system.service;

import com.neuedu.system.mapper.OrderInfoMapper;
import com.neuedu.system.service.impl.OrderInfoServiceImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class OrderStatisticsServiceTest {
    @Test
    public void filtersTotalsAndDailyStatisticsByUser() throws Exception {
        OrderInfoServiceImpl service = new OrderInfoServiceImpl();
        Map<String, Long> requestedUserIds = new HashMap<>();
        OrderInfoMapper mapper = proxy(OrderInfoMapper.class, (method, args) -> {
            if ("selectOrderStatistics".equals(method)) {
                requestedUserIds.put("totals", (Long) args[2]);
                return new HashMap<String, Object>();
            }
            if ("selectDailyOrderStatistics".equals(method)) {
                requestedUserIds.put("daily", (Long) args[2]);
                return Collections.emptyList();
            }
            return null;
        });
        Field field = OrderInfoServiceImpl.class.getDeclaredField("orderInfoMapper");
        field.setAccessible(true);
        field.set(service, mapper);

        service.getUserOrderStatistics(71L);

        assertEquals(Long.valueOf(71L), requestedUserIds.get("totals"));
        assertEquals(Long.valueOf(71L), requestedUserIds.get("daily"));
    }

    private interface ProxyCall {
        Object invoke(String method, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
