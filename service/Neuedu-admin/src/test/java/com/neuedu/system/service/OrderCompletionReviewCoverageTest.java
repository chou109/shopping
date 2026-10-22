package com.neuedu.system.service;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.domain.OrderDetail;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.ProductComment;
import com.neuedu.system.mapper.OrderInfoMapper;
import com.neuedu.system.mapper.ProductCommentMapper;
import com.neuedu.system.service.impl.OrderInfoServiceImpl;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OrderCompletionReviewCoverageTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void refusesToCompleteWhileAnyOrderProductHasNoReview() throws Exception {
        authenticate(42L);
        int[] updates = {0};
        OrderInfoServiceImpl service = service(orders(updates), comments(productId -> productId == 7L),
                Arrays.asList(detail(7L), detail(8L)));

        try {
            service.completeOrder("order-1");
            fail("Expected incomplete product review to block order completion");
        } catch (RuntimeException expected) {
            assertTrue(expected.getMessage().contains("全部商品"));
        }
        assertEquals(0, updates[0]);
    }

    @Test
    public void completesOwnedReviewEligibleOrderAfterEveryProductWasReviewed() throws Exception {
        authenticate(42L);
        int[] updates = {0};
        OrderInfoServiceImpl service = service(orders(updates), comments(productId -> true),
                Arrays.asList(detail(7L), detail(8L)));

        assertEquals(1, service.completeOrder("order-1"));
        assertEquals(1, updates[0]);
    }

    private static OrderInfoServiceImpl service(OrderInfoMapper orders, ProductCommentMapper comments,
                                                 java.util.List<OrderDetail> details) throws Exception {
        OrderInfoServiceImpl service = new OrderInfoServiceImpl();
        setField(service, "orderInfoMapper", orders);
        setField(service, "productCommentMapper", comments);
        setField(service, "orderDetailService", proxy(IOrderDetailService.class, (method, args) ->
                "selectOrderDetailByOrderNo".equals(method) ? details : defaultValue(method)));
        return service;
    }

    private static OrderInfoMapper orders(int[] updates) {
        return proxy(OrderInfoMapper.class, (method, args) -> {
            if ("lockOrderForCompletion".equals(method)) return order();
            if ("updateOrderInfo".equals(method)) {
                updates[0]++;
                return 1;
            }
            return defaultValue(method);
        });
    }

    private static ProductCommentMapper comments(java.util.function.LongPredicate reviewed) {
        return proxy(ProductCommentMapper.class, (method, args) -> {
            if ("selectReviewForOrderProduct".equals(method)) {
                if (!reviewed.test((Long) args[1])) return null;
                return new ProductComment();
            }
            return defaultValue(method);
        });
    }

    private static OrderInfo order() {
        OrderInfo order = new OrderInfo();
        order.setId(9L);
        order.setOrderNo("order-1");
        order.setUserId(42L);
        order.setStatus(OrderStatusConstants.WAIT_REVIEW);
        return order;
    }

    private static OrderDetail detail(Long productId) {
        OrderDetail detail = new OrderDetail();
        detail.setProductId(productId);
        return detail;
    }

    private static void authenticate(Long userId) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        LoginUser loginUser = new LoginUser(userId, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                loginUser, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private interface MapperCall {
        Object invoke(String methodName, Object[] args);
    }

    private static <T> T proxy(Class<T> type, MapperCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(String method) {
        switch (method) {
            case "toString": return "test mapper";
            default: return null;
        }
    }
}
