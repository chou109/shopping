package com.neuedu.system.controller;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.domain.UserAddress;
import com.neuedu.system.service.IUserAddressService;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class UserAddressOwnershipControllerTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void ignoresClientSuppliedOwnerWhenCreatingAddress() {
        UserAddress[] inserted = new UserAddress[1];
        IUserAddressService service = proxy((method, args) -> {
            if ("validateAddress".equals(method)) return true;
            if ("insertUserAddress".equals(method)) {
                inserted[0] = (UserAddress) args[0];
                return 1;
            }
            return null;
        });
        UserAddressController controller = new UserAddressController(service);
        SysUser user = new SysUser();
        user.setUserId(17L);
        LoginUser loginUser = new LoginUser(17L, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

        UserAddress request = new UserAddress();
        request.setCreateUser(99L);
        request.setUserId(99L);
        controller.add(request);

        assertEquals(Long.valueOf(17L), inserted[0].getCreateUser());
        assertEquals(Long.valueOf(17L), inserted[0].getUserId());
        assertEquals(Long.valueOf(17L), inserted[0].getUpdateUser());
    }

    private interface ServiceCall {
        Object invoke(String methodName, Object[] args);
    }

    private static IUserAddressService proxy(ServiceCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return (IUserAddressService) Proxy.newProxyInstance(
            IUserAddressService.class.getClassLoader(), new Class<?>[]{IUserAddressService.class}, handler);
    }
}
