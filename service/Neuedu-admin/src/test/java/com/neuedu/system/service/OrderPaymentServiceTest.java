package com.neuedu.system.service;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.mapper.OrderInfoMapper;
import com.neuedu.system.service.impl.OrderInfoServiceImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OrderPaymentServiceTest {
    @Test
    public void doesNotDeductBalanceWhenConditionalOrderUpdateLosesRace() throws Exception {
        PaymentFixture fixture = new PaymentFixture(0, true, "100.00", "20.00");

        assertEquals(0, fixture.service.payOrder("order-1", OrderStatusConstants.PAYMENT_BALANCE));

        assertEquals(0, fixture.deductionCalls);
        assertFalse(fixture.calls.contains("deduct-balance"));
    }

    @Test
    public void claimsOrderBeforeAtomicallyDeductingBalance() throws Exception {
        PaymentFixture fixture = new PaymentFixture(1, true, "100.00", "20.00");

        assertEquals(1, fixture.service.payOrder("order-1", OrderStatusConstants.PAYMENT_BALANCE));

        assertEquals(1, fixture.deductionCalls);
        assertTrue(fixture.calls.indexOf("claim-order") < fixture.calls.indexOf("deduct-balance"));
    }

    @Test
    public void doesNotClaimOrderWhenBalanceIsAlreadyInsufficient() throws Exception {
        PaymentFixture fixture = new PaymentFixture(1, true, "5.00", "20.00");

        try {
            fixture.service.payOrder("order-1", OrderStatusConstants.PAYMENT_BALANCE);
            fail("Expected insufficient balance to be rejected");
        } catch (RuntimeException error) {
            assertTrue(error.getMessage().contains("余额不足"));
        }

        assertFalse(fixture.calls.contains("claim-order"));
        assertEquals(0, fixture.deductionCalls);
    }

    @Test
    public void rejectsPaymentWhenAtomicBalanceDeductionFails() throws Exception {
        PaymentFixture fixture = new PaymentFixture(1, false, "100.00", "20.00");

        try {
            fixture.service.payOrder("order-1", OrderStatusConstants.PAYMENT_BALANCE);
            fail("Expected the atomic deduction failure to be rejected");
        } catch (RuntimeException error) {
            assertEquals("余额不足，请刷新后重试", error.getMessage());
        }

        assertEquals(1, fixture.deductionCalls);
    }

    private static class PaymentFixture {
        private final OrderInfoServiceImpl service = new OrderInfoServiceImpl();
        private final List<String> calls = new ArrayList<>();
        private int deductionCalls;

        private PaymentFixture(int orderUpdateResult, boolean deductionSucceeds,
                               String balance, String payment) throws Exception {
            OrderInfo order = new OrderInfo();
            order.setOrderNo("order-1");
            order.setStatus(OrderStatusConstants.WAIT_PAY);
            order.setUserId(7L);
            order.setPayment(new BigDecimal(payment));

            SysUser user = new SysUser();
            user.setBalance(new BigDecimal(balance));

            inject("orderInfoMapper", proxy(OrderInfoMapper.class, (method, args) -> {
                if ("selectOrderInfoByOrderNo".equals(method)) return order;
                if ("payOrder".equals(method)) {
                    calls.add("claim-order");
                    return orderUpdateResult;
                }
                return null;
            }));
            inject("sysUserService", proxy(ISysUserService.class, (method, args) -> {
                if ("selectUserById".equals(method)) {
                    calls.add("read-balance");
                    return user;
                }
                if ("deductUserBalance".equals(method)) {
                    calls.add("deduct-balance");
                    deductionCalls++;
                    return deductionSucceeds;
                }
                return null;
            }));
            inject("orderDetailService", proxy(IOrderDetailService.class, (method, args) ->
                "selectOrderDetailByOrderNo".equals(method) ? Collections.emptyList() : null));
        }

        private void inject(String fieldName, Object value) throws Exception {
            Field field = OrderInfoServiceImpl.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(service, value);
        }
    }

    private interface ProxyCall {
        Object invoke(String methodName, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
