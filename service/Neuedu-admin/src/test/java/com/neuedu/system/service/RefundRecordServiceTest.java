package com.neuedu.system.service;

import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.domain.OrderDetail;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.RefundRecord;
import com.neuedu.system.mapper.RefundRecordMapper;
import com.neuedu.system.service.impl.RefundRecordServiceImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class RefundRecordServiceTest {
    @Test
    public void creditsBalanceAndRestoresStockOnlyAfterAtomicTransition() throws Exception {
        RefundFixture fixture = new RefundFixture(1);

        assertEquals(1, fixture.service.processRefund("refund-1", OrderStatusConstants.REFUND_SUCCESS, "通过"));

        assertEquals(1, fixture.balanceUpdates);
        assertEquals(1, fixture.stockUpdates);
        assertEquals(1, fixture.orderUpdates);
    }

    @Test
    public void doesNotRepeatRefundSideEffectsWhenAnotherRequestAlreadyTransitioned() throws Exception {
        RefundFixture fixture = new RefundFixture(0);

        try {
            fixture.service.processRefund("refund-1", OrderStatusConstants.REFUND_SUCCESS, "通过");
            fail("Expected an already-processed error");
        } catch (RuntimeException error) {
            assertEquals("退款单已处理，请刷新后重试", error.getMessage());
        }

        assertEquals(0, fixture.balanceUpdates);
        assertEquals(0, fixture.stockUpdates);
        assertEquals(0, fixture.orderUpdates);
    }

    @Test
    public void cannotMarkUnsupportedPaymentAsRefunded() throws Exception {
        assertRefundRejected(new RefundFixture(1, OrderStatusConstants.PAYMENT_WECHAT, new BigDecimal("19.90")),
            "当前仅支持余额支付订单退款，尚未接入原路退款");
    }

    @Test
    public void cannotCancelWholeOrderForPartialRefund() throws Exception {
        assertRefundRejected(new RefundFixture(1, OrderStatusConstants.PAYMENT_BALANCE, new BigDecimal("5.00")),
            "当前订单仅支持全额退款");
    }

    private void assertRefundRejected(RefundFixture fixture, String message) {
        try {
            fixture.service.processRefund("refund-1", OrderStatusConstants.REFUND_SUCCESS, "通过");
            fail("Expected refund to be rejected");
        } catch (RuntimeException error) {
            assertEquals(message, error.getMessage());
        }

        assertEquals(0, fixture.statusTransitions);
        assertEquals(0, fixture.balanceUpdates);
        assertEquals(0, fixture.stockUpdates);
        assertEquals(0, fixture.orderUpdates);
    }

    private static class RefundFixture {
        private final RefundRecordServiceImpl service = new RefundRecordServiceImpl();
        private int balanceUpdates;
        private int stockUpdates;
        private int orderUpdates;
        private int statusTransitions;

        private RefundFixture(int transitionResult) throws Exception {
            this(transitionResult, OrderStatusConstants.PAYMENT_BALANCE, new BigDecimal("19.90"));
        }

        private RefundFixture(int transitionResult, Integer paymentType, BigDecimal refundAmount) throws Exception {
            RefundRecord record = new RefundRecord();
            record.setRefundNo("refund-1");
            record.setOrderNo("order-1");
            record.setUserId(7L);
            record.setRefundAmount(refundAmount);

            OrderDetail detail = new OrderDetail();
            detail.setProductId(42L);
            detail.setQuantity(2L);
            List<OrderDetail> details = Collections.singletonList(detail);

            OrderInfo order = new OrderInfo();
            order.setOrderNo("order-1");
            order.setUserId(7L);
            order.setPaymentType(paymentType);
            order.setPayment(new BigDecimal("19.90"));
            order.setOrderDetails(details);

            inject("refundRecordMapper", proxy(RefundRecordMapper.class, (method, args) -> {
                if ("selectRefundRecordByRefundNo".equals(method)) return record;
                if ("transitionRefundStatus".equals(method)) {
                    statusTransitions++;
                    return transitionResult;
                }
                return null;
            }));
            inject("orderInfoService", proxy(IOrderInfoService.class, (method, args) -> {
                if ("selectOrderInfoByOrderNo".equals(method)) return order;
                if ("updateOrderInfo".equals(method)) {
                    orderUpdates++;
                    return 1;
                }
                return null;
            }));
            inject("sysUserService", proxy(ISysUserService.class, (method, args) -> {
                if ("updateUserBalance".equals(method)) {
                    balanceUpdates++;
                    return true;
                }
                return null;
            }));
            inject("productService", proxy(IProductService.class, (method, args) -> {
                if ("updateProductStock".equals(method)) {
                    stockUpdates++;
                    return 1;
                }
                return null;
            }));
        }

        private void inject(String fieldName, Object value) throws Exception {
            Field field = RefundRecordServiceImpl.class.getDeclaredField(fieldName);
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
