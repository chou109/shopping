package com.neuedu.web.service;

import com.neuedu.common.core.domain.entity.SysRole;
import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PermissionServiceTest {
    private final PermissionService permissionService = new PermissionService();

    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void deniesPermissionAndRoleChecksWithoutAnAuthenticatedLoginUser() {
        SecurityContextHolder.clearContext();

        assertFalse(permissionService.hasPermi("system:user:list"));
        assertFalse(permissionService.hasRole("admin"));
    }

    @Test
    public void checksExplicitPermissionsAndWildcardInsteadOfAllowingEverything() {
        authenticate(user(Collections.singleton("system:user:list"), Collections.emptyList()));

        assertTrue(permissionService.hasPermi("system:user:list"));
        assertFalse(permissionService.hasPermi("system:user:remove"));
        assertTrue(permissionService.hasAnyPermi("system:user:remove,system:user:list"));
        assertTrue(permissionService.lacksPermi("system:user:remove"));

        authenticate(user(Collections.singleton("*:*:*"), Collections.emptyList()));
        assertTrue(permissionService.hasPermi("system:user:remove"));
    }

    @Test
    public void checksExplicitRolesAndKeepsAdministratorRoleAsSuperuser() {
        authenticate(user(Collections.emptySet(), Collections.singletonList(role("merchant"))));

        assertTrue(permissionService.hasRole("merchant"));
        assertFalse(permissionService.hasRole("admin"));
        assertTrue(permissionService.hasAnyRoles("guest,merchant"));
        assertTrue(permissionService.lacksRole("admin"));

        authenticate(user(Collections.emptySet(), Collections.singletonList(role("admin"))));
        assertTrue(permissionService.hasRole("merchant"));
    }

    private void authenticate(LoginUser user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    private LoginUser user(java.util.Set<String> permissions, java.util.List<SysRole> roles) {
        SysUser sysUser = new SysUser();
        sysUser.setRoles(roles);
        return new LoginUser(sysUser, permissions);
    }

    private SysRole role(String roleKey) {
        SysRole role = new SysRole();
        role.setRoleKey(roleKey);
        return role;
    }
}
