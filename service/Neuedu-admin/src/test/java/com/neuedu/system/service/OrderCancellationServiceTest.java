package com.neuedu.system.service;

import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.domain.OrderDetail;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.mapper.OrderInfoMapper;
import com.neuedu.system.service.impl.OrderInfoServiceImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OrderCancellationServiceTest {
    @Test
    public void restoresInventoryOnlyAfterPendingPaymentCancellationSucceeds() throws Exception {
        CancellationFixture fixture = new CancellationFixture(OrderStatusConstants.WAIT_PAY, 1);

        assertEquals(1, fixture.service.cancelOrder("order-1", "用户取消"));

        assertEquals(1, fixture.cancelAttempts);
        assertEquals(1, fixture.stockUpdates);
        assertEquals(Long.valueOf(2L), fixture.restoredQuantity);
    }

    @Test
    public void doesNotRestoreInventoryWhenConditionalCancellationFails() throws Exception {
        CancellationFixture fixture = new CancellationFixture(OrderStatusConstants.WAIT_PAY, 0);

        assertEquals(0, fixture.service.cancelOrder("order-1", "用户取消"));

        assertEquals(1, fixture.cancelAttempts);
        assertEquals(0, fixture.stockUpdates);
    }

    @Test
    public void requiresPaidOrdersToUseTheRefundFlow() throws Exception {
        CancellationFixture fixture = new CancellationFixture(OrderStatusConstants.WAIT_SHIP, 1);

        try {
            fixture.service.cancelOrder("order-1", "用户取消");
            fail("Expected paid order cancellation to be rejected");
        } catch (RuntimeException error) {
            assertTrue(error.getMessage().contains("请使用退款功能"));
        }

        assertEquals(0, fixture.cancelAttempts);
        assertEquals(0, fixture.stockUpdates);
    }

    private static class CancellationFixture {
        private final OrderInfoServiceImpl service = new OrderInfoServiceImpl();
        private int cancelAttempts;
        private int stockUpdates;
        private Long restoredQuantity;

        private CancellationFixture(Integer status, int cancelResult) throws Exception {
            OrderInfo order = new OrderInfo();
            order.setStatus(status);

            OrderDetail detail = new OrderDetail();
            detail.setProductId(42L);
            detail.setQuantity(2L);
            List<OrderDetail> details = Collections.singletonList(detail);

            inject("orderInfoMapper", proxy(OrderInfoMapper.class, (method, args) -> {
                if ("selectOrderInfoByOrderNo".equals(method)) return order;
                if ("cancelOrder".equals(method)) {
                    cancelAttempts++;
                    return cancelResult;
                }
                return null;
            }));
            inject("orderDetailService", proxy(IOrderDetailService.class, (method, args) ->
                "selectOrderDetailByOrderNo".equals(method) ? details : null));
            inject("productService", proxy(IProductService.class, (method, args) -> {
                if ("updateProductStock".equals(method)) {
                    stockUpdates++;
                    restoredQuantity = (Long) args[1];
                    return 1;
                }
                return null;
            }));
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
