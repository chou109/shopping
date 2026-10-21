package com.neuedu.system.service;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.system.mapper.SysUserMapper;
import com.neuedu.system.service.impl.SysUserServiceImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class SysUserBalanceServiceTest {
    @Test
    public void appliesBalanceChangesAsAtomicDeltas() throws Exception {
        BalanceFixture fixture = new BalanceFixture("100.00", 1);

        assertTrue(fixture.service.updateUserBalance(7L, new BigDecimal("12.50")));

        assertEquals(new BigDecimal("12.50"), fixture.adjustment);
    }

    @Test
    public void rejectsAnAtomicAdjustmentWhenConcurrentBalanceChangesBreakLimits() throws Exception {
        BalanceFixture fixture = new BalanceFixture("100.00", 0);

        try {
            fixture.service.updateUserBalance(7L, new BigDecimal("12.50"));
            fail("Expected concurrent balance change to be rejected");
        } catch (RuntimeException error) {
            assertEquals("余额已发生变化，请刷新后重试", error.getMessage());
        }
    }

    @Test
    public void validatesInsufficientBalanceBeforeAttemptingAdjustment() throws Exception {
        BalanceFixture fixture = new BalanceFixture("5.00", 1);

        try {
            fixture.service.updateUserBalance(7L, new BigDecimal("-10.00"));
            fail("Expected insufficient balance to be rejected");
        } catch (RuntimeException error) {
            assertEquals("用户余额不足", error.getMessage());
        }

        assertEquals(null, fixture.adjustment);
    }

    private static class BalanceFixture {
        private final SysUserServiceImpl service = new SysUserServiceImpl();
        private BigDecimal adjustment;

        private BalanceFixture(String balance, int updateResult) throws Exception {
            SysUser user = new SysUser();
            user.setBalance(new BigDecimal(balance));
            SysUserMapper mapper = proxy((method, args) -> {
                if ("selectUserById".equals(method)) return user;
                if ("adjustUserBalance".equals(method)) {
                    adjustment = (BigDecimal) args[1];
                    return updateResult;
                }
                return null;
            });

            Field field = SysUserServiceImpl.class.getDeclaredField("userMapper");
            field.setAccessible(true);
            field.set(service, mapper);
        }
    }

    private interface ProxyCall {
        Object invoke(String methodName, Object[] args);
    }

    private static SysUserMapper proxy(ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return (SysUserMapper) Proxy.newProxyInstance(
            SysUserMapper.class.getClassLoader(), new Class<?>[]{SysUserMapper.class}, handler);
    }
}
