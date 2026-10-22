package com.neuedu.system.controller;

import com.neuedu.common.core.domain.entity.SysRole;
import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.domain.Cart;
import com.neuedu.system.service.ICartService;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class CartControllerSecurityTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void consumerCartActionsUseConsumerRoleInsteadOfAdminMenuPermissions() throws Exception {
        for (String name : new String[]{"list", "getInfo", "add", "edit", "remove", "removeBatch", "clear", "count", "batchSelect"}) {
            PreAuthorize authorize = findMethod(name).getAnnotation(PreAuthorize.class);
            assertNotNull(name, authorize);
            assertEquals(name, "@ss.hasRole('common')", authorize.value());
        }
        assertEquals("@ss.hasPermi('system:cart:export')",
                findMethod("export").getAnnotation(PreAuthorize.class).value());
    }

    @Test
    public void customerCannotEditAnotherUsersCartOrTransferOwnership() {
        authenticate(42L, "common");
        Cart existing = new Cart();
        existing.setId(9L);
        existing.setUserId(42L);
        existing.setProductId(77L);
        existing.setActivityId(12L);
        existing.setActivityName("活动");
        existing.setCreateUser(42L);
        existing.setDeleteFlag(0);

        Cart[] saved = new Cart[1];
        ICartService service = serviceProxy(existing, saved);
        CartController controller = new CartController(service);
        Cart request = new Cart();
        request.setUserId(999L);
        request.setProductId(88L);
        request.setActivityId(99L);
        request.setCreateUser(999L);
        request.setDeleteFlag(1);
        request.setQuantity(2);

        controller.edit(9L, request);

        assertNotNull(saved[0]);
        assertEquals(Long.valueOf(42L), saved[0].getUserId());
        assertEquals(Long.valueOf(77L), saved[0].getProductId());
        assertEquals(Long.valueOf(12L), saved[0].getActivityId());
        assertEquals(Long.valueOf(42L), saved[0].getCreateUser());
        assertEquals(Integer.valueOf(0), saved[0].getDeleteFlag());
        assertEquals(Long.valueOf(42L), saved[0].getUpdateUser());
    }

    @Test
    public void customerCannotEditCartOwnedBySomeoneElse() {
        authenticate(42L, "common");
        Cart existing = new Cart();
        existing.setId(9L);
        existing.setUserId(43L);
        Cart[] saved = new Cart[1];

        new CartController(serviceProxy(existing, saved)).edit(9L, new Cart());

        assertNull(saved[0]);
    }

    private java.lang.reflect.Method findMethod(String name) {
        for (java.lang.reflect.Method method : CartController.class.getDeclaredMethods()) {
            if (method.getName().equals(name)) return method;
        }
        throw new AssertionError("Missing controller method: " + name);
    }

    private void authenticate(Long userId, String roleKey) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        user.setRoles(Collections.singletonList(role));
        LoginUser loginUser = new LoginUser(user, Collections.<String>emptySet());
        loginUser.setUserId(userId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities()));
    }

    private ICartService serviceProxy(Cart existing, Cart[] saved) {
        InvocationHandler handler = (proxy, method, args) -> {
            if ("selectCartById".equals(method.getName())) return existing;
            if ("updateCart".equals(method.getName())) {
                saved[0] = (Cart) args[0];
                return 1;
            }
            if (method.getReturnType().equals(boolean.class)) return false;
            if (method.getReturnType().equals(int.class)) return 0;
            if (method.getReturnType().equals(long.class)) return 0L;
            return null;
        };
        return (ICartService) Proxy.newProxyInstance(ICartService.class.getClassLoader(),
                new Class<?>[]{ICartService.class}, handler);
    }
}
