package com.neuedu.web.service;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.entity.SysRole;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SysLoginServiceAdminClientTest {
    @Test
    public void treatsFirstRegularAccountAsConsumerInsteadOfAdministrator() {
        SysUser consumer = new SysUser();
        consumer.setUserId(2L);
        consumer.setUserType(0);

        assertFalse(SysLoginService.isAdminAccount(consumer));
    }

    @Test
    public void recognizesSuperAdministratorByIdAndOtherAdministratorsByUserType() {
        SysUser superAdministrator = new SysUser();
        superAdministrator.setUserId(1L);
        superAdministrator.setUserType(0);

        SysUser administrator = new SysUser();
        administrator.setUserId(3L);
        administrator.setUserType(1);

        assertTrue(SysLoginService.isAdminAccount(superAdministrator));
        assertTrue(SysLoginService.isAdminAccount(administrator));
    }

    @Test
    public void allowsMerchantRoleInManagementConsoleWithoutTreatingItAsAdministrator() {
        SysRole merchantRole = new SysRole();
        merchantRole.setRoleKey("shang");
        SysUser merchant = new SysUser();
        merchant.setUserId(4L);
        merchant.setUserType(0);
        merchant.setRoles(Collections.singletonList(merchantRole));

        assertFalse(SysLoginService.isAdminAccount(merchant));
        assertTrue(SysLoginService.isMerchantAccount(merchant));
    }
}
