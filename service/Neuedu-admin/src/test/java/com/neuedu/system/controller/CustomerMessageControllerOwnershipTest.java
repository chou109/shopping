package com.neuedu.system.controller;

import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.domain.CustomerMessage;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.ICustomerMessageService;
import com.neuedu.system.service.IShopService;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CustomerMessageControllerOwnershipTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void shopOwnerCannotReadAnotherShopsConversation() {
        setCurrentUser(42L);
        AtomicReference<String> queriedSession = new AtomicReference<>();
        CustomerMessageController controller = controllerFor(
            (method, args) -> {
                if ("selectShopByUserId".equals(method)) return new Shop();
                throw new AssertionError("Unexpected shop service call: " + method);
            },
            (method, args) -> {
                if ("isAdminSessionOwned".equals(method)) return false;
                if ("getAdminSessionMessages".equals(method)) queriedSession.set((String) args[0]);
                return null;
            });

        AjaxResult result = controller.getSessionMessages("CS_9_TO_99");

        assertEquals(403, result.get("code"));
        assertNull(queriedSession.get());
    }

    @Test
    public void shopOwnerSessionListIsScopedToCurrentUser() {
        setCurrentUser(42L);
        AtomicReference<Long> scope = new AtomicReference<>();
        CustomerMessageController controller = controllerFor(
            (method, args) -> "selectShopByUserId".equals(method) ? new Shop() : null,
            (method, args) -> {
                if ("getAdminSessionList".equals(method)) scope.set((Long) args[0]);
                return Collections.emptyList();
            });

        controller.getAdminSessions();

        assertEquals(Long.valueOf(42L), scope.get());
    }

    @Test
    public void systemAdministratorGetsGlobalSessionScope() {
        setCurrentUser(1L);
        AtomicReference<Long> scope = new AtomicReference<>(42L);
        CustomerMessageController controller = controllerFor(
            (method, args) -> { throw new AssertionError("Admin must not require shop lookup"); },
            (method, args) -> {
                if ("getAdminSessionList".equals(method)) scope.set((Long) args[0]);
                return Collections.emptyList();
            });

        controller.getAdminSessions();

        assertNull(scope.get());
    }

    @Test
    public void systemAdministratorReplyKeepsActualSenderId() {
        setCurrentUser(1L);
        AtomicReference<Long> senderId = new AtomicReference<>();
        CustomerMessageController controller = controllerFor(
            (method, args) -> { throw new AssertionError("Admin must not require shop lookup"); },
            (method, args) -> {
                if ("isAdminSessionOwned".equals(method)) return true;
                if ("sendMessageToUser".equals(method)) {
                    senderId.set((Long) args[1]);
                    return new CustomerMessage();
                }
                return null;
            });
        Map<String, Object> params = new HashMap<>();
        params.put("sessionId", "CS_9_TO_99");
        params.put("content", "您好");

        controller.adminReplyMessage(params);

        assertEquals(Long.valueOf(1L), senderId.get());
    }

    @Test(expected = org.springframework.security.access.AccessDeniedException.class)
    public void ordinaryUserCannotOpenMerchantInbox() {
        setCurrentUser(84L);
        CustomerMessageController controller = controllerFor(
            (method, args) -> null,
            (method, args) -> null);

        controller.getAdminSessions();
    }

    private CustomerMessageController controllerFor(ServiceCall shopsCall, ServiceCall messagesCall) {
        IShopService shops = proxy(IShopService.class, shopsCall);
        ICustomerMessageService messages = proxy(ICustomerMessageService.class, messagesCall);
        return new CustomerMessageController(messages, shops);
    }

    private void setCurrentUser(Long userId) {
        SysUser user = new SysUser();
        user.setUserName("test-user");
        LoginUser loginUser = new LoginUser(userId, null, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));
    }

    private interface ServiceCall {
        Object invoke(String methodName, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ServiceCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
