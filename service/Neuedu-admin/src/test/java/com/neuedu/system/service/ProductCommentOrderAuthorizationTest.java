package com.neuedu.system.service;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.common.exception.ServiceException;
import com.neuedu.common.utils.SecurityUtils;
import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.Product;
import com.neuedu.system.domain.ProductComment;
import com.neuedu.system.mapper.OrderInfoMapper;
import com.neuedu.system.mapper.ProductCommentMapper;
import com.neuedu.system.service.impl.ProductCommentServiceImpl;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ProductCommentOrderAuthorizationTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void rejectsOrderThatDoesNotBelongToCurrentUserOrContainProduct() {
        authenticate(42L);
        int[] inserts = {0};
        ProductCommentServiceImpl service = service(null, (method, args) -> {
            if ("insertProductComment".equals(method)) inserts[0]++;
            return defaultValue(method);
        });

        expectServiceException(() -> service.publishComment(comment()));
        assertEquals(0, inserts[0]);
    }

    @Test
    public void allowsNetworkRetryForExistingReviewEvenAfterOrderWasCompleted() {
        authenticate(42L);
        int[] inserts = {0};
        ProductComment existing = new ProductComment();
        existing.setCommentId(88L);
        ProductCommentServiceImpl service = service(order(OrderStatusConstants.COMPLETED), (method, args) -> {
            if ("selectReviewForOrderProduct".equals(method)) return existing;
            if ("insertProductComment".equals(method)) inserts[0]++;
            return defaultValue(method);
        });

        assertTrue(service.publishComment(comment()));
        assertEquals(0, inserts[0]);
    }

    @Test
    public void requiresReviewEligibleOrderBeforeCreatingReview() {
        authenticate(42L);
        int[] inserts = {0};
        ProductCommentServiceImpl service = service(order(OrderStatusConstants.WAIT_RECEIVE), (method, args) -> {
            if ("insertProductComment".equals(method)) inserts[0]++;
            return defaultValue(method);
        });

        expectServiceException(() -> service.publishComment(comment()));
        assertEquals(0, inserts[0]);
    }

    @Test
    public void createsReviewForOwnedProductOnReviewEligibleOrder() {
        authenticate(42L);
        ProductComment submitted = comment();
        int[] inserts = {0};
        ProductCommentServiceImpl service = service(order(OrderStatusConstants.WAIT_REVIEW), (method, args) -> {
            if ("insertProductComment".equals(method)) {
                inserts[0]++;
                return 1;
            }
            return defaultValue(method);
        });

        assertTrue(service.publishComment(submitted));
        assertEquals(1, inserts[0]);
        assertEquals(Long.valueOf(42L), submitted.getUserId());
    }

    @Test
    public void requiresValidRatingAndOrderId() {
        authenticate(42L);
        ProductCommentServiceImpl service = service(order(OrderStatusConstants.WAIT_REVIEW), (method, args) -> defaultValue(method));

        ProductComment missingOrder = comment();
        missingOrder.setOrderId(null);
        expectServiceException(() -> service.publishComment(missingOrder));
        ProductComment invalidScore = comment();
        invalidScore.setScore(6);
        expectServiceException(() -> service.publishComment(invalidScore));
    }

    private static ProductCommentServiceImpl service(OrderInfo lockedOrder, ProductCommentCall commentCall) {
        ProductCommentMapper comments = proxy(ProductCommentMapper.class, (method, args) -> commentCall.invoke(method, args));
        ProductCommentServiceImpl service = new ProductCommentServiceImpl(comments,
                proxy(com.neuedu.system.service.IProductService.class, (method, args) ->
                        "selectProductById".equals(method) ? new Product() : defaultValue(method)), null);
        OrderInfoMapper orders = proxy(OrderInfoMapper.class, (method, args) ->
                "lockOrderForProductReview".equals(method) ? lockedOrder : defaultValue(method));
        try {
            Field field = ProductCommentServiceImpl.class.getDeclaredField("orderInfoMapper");
            field.setAccessible(true);
            field.set(service, orders);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError(error);
        }
        return service;
    }

    private static ProductComment comment() {
        ProductComment comment = new ProductComment();
        comment.setProductId(7L);
        comment.setOrderId(9L);
        comment.setScore(5);
        comment.setParentId(1L);
        comment.setContent("评价内容");
        return comment;
    }

    private static OrderInfo order(Integer status) {
        OrderInfo order = new OrderInfo();
        order.setId(9L);
        order.setUserId(42L);
        order.setStatus(status);
        return order;
    }

    private static void authenticate(Long userId) {
        SysUser user = new SysUser();
        user.setUserId(userId);
        LoginUser loginUser = new LoginUser(userId, 1L, user, Collections.emptySet());
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                loginUser, null, AuthorityUtils.createAuthorityList("ROLE_USER")));
    }

    private static void expectServiceException(Runnable action) {
        try {
            action.run();
            fail("Expected ServiceException");
        } catch (ServiceException expected) {
            // expected
        }
    }

    private interface ProductCommentCall {
        Object invoke(String method, Object[] args);
    }

    private interface MapperCall {
        Object invoke(String method, Object[] args);
    }

    private static <T> T proxy(Class<T> type, MapperCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(String method) {
        switch (method) {
            case "insertProductComment": return 1;
            case "toString": return "test mapper";
            default: return null;
        }
    }
}
