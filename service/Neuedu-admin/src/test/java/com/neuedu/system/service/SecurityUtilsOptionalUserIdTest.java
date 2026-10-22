package com.neuedu.system.service;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.common.utils.SecurityUtils;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SecurityUtilsOptionalUserIdTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void returnsNullWithoutAuthenticationOrForAnAnonymousPrincipal() {
        assertNull(SecurityUtils.getOptionalUserId());
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
            "test-key", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));
        assertNull(SecurityUtils.getOptionalUserId());
    }

    @Test
    public void returnsTheAuthenticatedLoginUserId() {
        SysUser user = new SysUser();
        user.setUserId(42L);
        LoginUser loginUser = new LoginUser(42L, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
            loginUser, null, AuthorityUtils.createAuthorityList("ROLE_USER")));

        assertEquals(Long.valueOf(42L), SecurityUtils.getOptionalUserId());
    }
}
