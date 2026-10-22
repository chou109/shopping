package com.neuedu.web.controller.system;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.service.ISysUserService;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class SysProfilePasswordValidationTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void rejectsMissingAndOutOfRangePasswordsWithoutUpdatingCredentials() throws Exception {
        AtomicInteger passwordUpdates = new AtomicInteger();
        SysProfileController controller = new SysProfileController();
        ISysUserService users = proxy((method, args) -> {
            if ("resetUserPwd".equals(method)) passwordUpdates.incrementAndGet();
            return 0;
        });
        setField(controller, "userService", users);
        installUser();

        assertEquals("原密码不能为空", message(controller.updatePwd(null, "newpass1")));
        assertEquals("新密码不能为空", message(controller.updatePwd("current1", null)));
        assertEquals("新密码长度必须在6到20个字符之间", message(controller.updatePwd("current1", "12345")));
        assertEquals("新密码长度必须在6到20个字符之间", message(controller.updatePwd("current1", "123456789012345678901")));
        assertEquals(0, passwordUpdates.get());
    }

    private static void installUser() {
        SysUser user = new SysUser();
        user.setUserId(17L);
        user.setUserName("test-user");
        user.setPassword(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("current1"));
        LoginUser loginUser = new LoginUser(17L, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    private static String message(Object result) {
        return (String) ((java.util.Map<?, ?>) result).get("msg");
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = SysProfileController.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private interface ServiceCall {
        Object invoke(String methodName, Object[] args);
    }

    private static ISysUserService proxy(ServiceCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return (ISysUserService) Proxy.newProxyInstance(
            ISysUserService.class.getClassLoader(), new Class<?>[]{ISysUserService.class}, handler);
    }
}
