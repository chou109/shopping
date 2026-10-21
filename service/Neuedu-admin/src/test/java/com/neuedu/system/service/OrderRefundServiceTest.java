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
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OrderRefundServiceTest {
    @Test
    public void doesNotRefundOrRestoreStockWhenOrderStateChangesBeforeRefund() throws Exception {
        RefundFixture fixture = new RefundFixture(0, false);

        try {
            fixture.service.applyRefund("order-1", "用户取消", null, null);
            fail("Expected a state-change error");
        } catch (RuntimeException error) {
            assertEquals("订单状态已变化，请刷新后重试", error.getMessage());
        }

        assertEquals(0, fixture.balanceUpdates);
        assertEquals(0, fixture.stockUpdates);
    }

    @Test
    public void refundsBalanceAndRestoresStockAfterSuccessfulConditionalUpdate() throws Exception {
        RefundFixture fixture = new RefundFixture(1, false);

        assertEquals(1, fixture.service.applyRefund("order-1", "用户取消", null, null));

        assertEquals(1, fixture.balanceUpdates);
        assertEquals(1, fixture.stockUpdates);
    }

    @Test
    public void rejectsBlindBoxRefundBeforeChangingOrderOrBalance() throws Exception {
        RefundFixture fixture = new RefundFixture(1, true);

        try {
            fixture.service.applyRefund("order-1", "用户取消", null, null);
            fail("Expected blind box refund to be rejected");
        } catch (RuntimeException error) {
            assertTrue(error.getMessage().contains("盲盒订单不支持退款"));
        }

        assertEquals(0, fixture.refundUpdates);
        assertEquals(0, fixture.balanceUpdates);
        assertEquals(0, fixture.stockUpdates);
    }

    private static class RefundFixture {
        private final OrderInfoServiceImpl service = new OrderInfoServiceImpl();
        private int refundUpdates;
        private int balanceUpdates;
        private int stockUpdates;

        private RefundFixture(int conditionalUpdateResult, boolean blindBox) throws Exception {
            OrderInfo order = new OrderInfo();
            order.setStatus(OrderStatusConstants.WAIT_SHIP);
            order.setUserId(7L);
            order.setPayment(new BigDecimal("19.90"));
            order.setIsBlindBox(blindBox);

            OrderDetail detail = new OrderDetail();
            detail.setProductId(42L);
            detail.setProductName(blindBox ? "【盲盒】商品" : "普通商品");
            detail.setQuantity(2L);
            List<OrderDetail> details = Collections.singletonList(detail);

            inject("orderInfoMapper", proxy(OrderInfoMapper.class, (method, args) -> {
                if ("selectOrderInfoByOrderNo".equals(method)) return order;
                if ("cancelOrderForRefund".equals(method)) {
                    refundUpdates++;
                    return conditionalUpdateResult;
                }
                return defaultValue(method);
            }));
            inject("orderDetailService", proxy(IOrderDetailService.class, (method, args) ->
                "selectOrderDetailByOrderNo".equals(method) ? details : defaultValue(method)));
            inject("sysUserService", proxy(ISysUserService.class, (method, args) -> {
                if ("updateUserBalance".equals(method)) {
                    balanceUpdates++;
                    return true;
                }
                return defaultValue(method);
            }));
            inject("productService", proxy(IProductService.class, (method, args) -> {
                if ("updateProductStock".equals(method)) {
                    stockUpdates++;
                    return 1;
                }
                return defaultValue(method);
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

    private static Object defaultValue(String methodName) {
        return null;
    }
}
