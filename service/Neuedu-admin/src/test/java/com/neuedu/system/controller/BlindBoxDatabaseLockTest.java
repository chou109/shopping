package com.neuedu.system.controller;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.domain.OrderDetail;
import com.neuedu.system.mapper.SysUserMapper;
import com.neuedu.system.service.IOrderDetailService;
import com.neuedu.system.service.IOrderInfoService;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BlindBoxDatabaseLockTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void locksUserRowBeforeCheckingBlindBoxHistoryInsideTransaction() throws Exception {
        List<String> calls = new ArrayList<>();
        SysUser user = new SysUser();
        user.setUserId(42L);
        LoginUser loginUser = new LoginUser(42L, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

        OrderInfoController controller = new OrderInfoController(proxy(IOrderInfoService.class, (name, args) -> null));
        setField(controller, "sysUserMapper", proxy(SysUserMapper.class, (name, args) -> {
            if ("lockUserForUpdate".equals(name)) {
                calls.add("lock");
                return 42L;
            }
            return null;
        }));
        OrderDetail purchased = new OrderDetail();
        purchased.setProductName("【盲盒】蓝牙耳机");
        setField(controller, "orderDetailService", proxy(IOrderDetailService.class, (name, args) -> {
            if ("selectOrderDetailByUserId".equals(name)) {
                calls.add("history");
                return Collections.singletonList(purchased);
            }
            return null;
        }));

        Transactional transaction = OrderInfoController.class
            .getMethod("createBlindBoxOrder", Long.class)
            .getAnnotation(Transactional.class);
        assertNotNull("the row lock must remain held through the order transaction", transaction);
        assertNull(controller.createBlindBoxOrder(7L).get("data"));
        assertEquals(java.util.Arrays.asList("lock", "history"), calls);
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private interface ProxyCall {
        Object invoke(String methodName, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
