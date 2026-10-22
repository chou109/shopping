package com.neuedu.system.demoai;

import org.junit.Test;

import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AIChatWebSocketPrivacyTest {
    @Test
    public void ignoresClientClaimedUserIdForOrderQueries() {
        List<String> sentMessages = new ArrayList<>();
        RemoteEndpoint.Basic remote = proxy(RemoteEndpoint.Basic.class, (instance, method, args) -> {
            if ("sendText".equals(method.getName())) sentMessages.add((String) args[0]);
            return null;
        });
        Session session = proxy(Session.class, (instance, method, args) -> {
            if ("getBasicRemote".equals(method.getName())) return remote;
            if ("isOpen".equals(method.getName())) return true;
            if ("getId".equals(method.getName())) return "privacy-test";
            return null;
        });

        AIChatWebSocket endpoint = new AIChatWebSocket();
        endpoint.onMessage("{\"question\":\"查看我的订单\",\"userId\":12345}", session);

        assertEquals(3, sentMessages.size());
        assertTrue(sentMessages.get(0).contains("为保护订单信息"));
        assertTrue(sentMessages.get(0).contains("只查询您自己的订单状态"));
        assertTrue(!sentMessages.get(0).contains("订单编号"));
        assertTrue(sentMessages.get(2).contains("\"type\":\"end\""));
    }

    @Test
    public void doesNotRevealAnOrderWhenAClientSuppliesItsNumber() {
        List<String> sentMessages = new ArrayList<>();
        RemoteEndpoint.Basic remote = proxy(RemoteEndpoint.Basic.class, (instance, method, args) -> {
            if ("sendText".equals(method.getName())) sentMessages.add((String) args[0]);
            return null;
        });
        Session session = proxy(Session.class, (instance, method, args) -> {
            if ("getBasicRemote".equals(method.getName())) return remote;
            if ("isOpen".equals(method.getName())) return true;
            if ("getId".equals(method.getName())) return "privacy-test-order-number";
            return null;
        });

        AIChatWebSocket endpoint = new AIChatWebSocket();
        endpoint.onMessage("{\"question\":\"查询订单 O12345678901234567890\"}", session);

        assertEquals(3, sentMessages.size());
        assertTrue(sentMessages.get(0).contains("为保护订单信息"));
        assertTrue(!sentMessages.get(0).contains("12345678901234567890"));
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
    }
}
