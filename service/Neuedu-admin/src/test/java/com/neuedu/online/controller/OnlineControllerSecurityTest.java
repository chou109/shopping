package com.neuedu.online.controller;

import com.neuedu.common.annotation.Anonymous;
import com.neuedu.common.core.domain.entity.SysRole;
import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OnlineControllerSecurityTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void dynamicSqlAndSchemaControllersAreNotAnonymous() {
        assertFalse(OnLineController.class.isAnnotationPresent(Anonymous.class));
        assertFalse(OnlineDbController.class.isAnnotationPresent(Anonymous.class));
        assertEquals("isAuthenticated()", OnLineController.class.getAnnotation(PreAuthorize.class).value());
        assertEquals("@ss.hasRole('admin')", OnlineDbController.class.getAnnotation(PreAuthorize.class).value());
    }

    @Test
    public void dynamicSqlPermissionTypesRequireMatchingPermissionsOrRoles() {
        OnLineController controller = new OnLineController();
        authenticate(user(Collections.singleton("system:user:list"), "merchant"));

        assertTrue(controller.checkPermission("hasPermi", "system:user:list"));
        assertFalse(controller.checkPermission("hasPermi", "system:user:remove"));
        assertTrue(controller.checkPermission("lacksPermi", "system:user:remove"));
        assertTrue(controller.checkPermission("hasAnyPermi", "system:user:remove,system:user:list"));
        assertTrue(controller.checkPermission("hasRole", "merchant"));
        assertFalse(controller.checkPermission("hasRole", "admin"));
        assertFalse(controller.checkPermission("unrecognized", "anything"));
    }

    @Test
    public void dynamicSqlCanSkipAdditionalChecksOnlyForAuthenticatedUsers() {
        OnLineController controller = new OnLineController();
        authenticate(user(Collections.emptySet(), "merchant"));
        assertTrue(controller.checkPermission("无", null));
    }

    private void authenticate(LoginUser user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private LoginUser user(java.util.Set<String> permissions, String roleKey) {
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        SysUser sysUser = new SysUser();
        sysUser.setRoles(Collections.singletonList(role));
        return new LoginUser(sysUser, permissions);
    }
}
