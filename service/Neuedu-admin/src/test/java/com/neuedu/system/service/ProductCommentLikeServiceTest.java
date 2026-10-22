package com.neuedu.system.service;

import com.neuedu.system.domain.ProductComment;
import com.neuedu.system.mapper.ProductCommentMapper;
import com.neuedu.system.service.impl.ProductCommentServiceImpl;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProductCommentLikeServiceTest {
    @Test
    public void incrementsCountOnlyWhenTheUserCreatesANewLike() {
        List<String> calls = new ArrayList<>();
        ProductCommentMapper comments = mapper(calls, 1, 1, 1);
        ProductCommentServiceImpl service = new ProductCommentServiceImpl(comments, null, null);

        assertTrue(service.likeComment(5L, 42L));
        assertEquals(java.util.Arrays.asList("read", "insert", "increment"), calls);
    }

    @Test
    public void duplicateLikeDoesNotIncrementTheAggregateAgain() {
        List<String> calls = new ArrayList<>();
        ProductCommentMapper comments = mapper(calls, 0, 1, 1);
        ProductCommentServiceImpl service = new ProductCommentServiceImpl(comments, null, null);

        assertFalse(service.likeComment(5L, 42L));
        assertEquals(java.util.Arrays.asList("read", "insert", "count"), calls);
    }

    @Test
    public void decrementsCountOnlyWhenAnExistingLikeWasRemoved() {
        List<String> calls = new ArrayList<>();
        ProductCommentMapper comments = mapper(calls, 1, 1, 1);
        ProductCommentServiceImpl service = new ProductCommentServiceImpl(comments, null, null);

        assertTrue(service.unlikeComment(5L, 42L));
        assertEquals(java.util.Arrays.asList("read", "delete", "decrement"), calls);
    }

    private static ProductCommentMapper mapper(List<String> calls, int inserted, int liked, int updated) {
        return proxy(ProductCommentMapper.class, (method, args) -> {
            switch (method) {
                case "selectProductCommentByCommentId":
                    calls.add("read");
                    ProductComment comment = new ProductComment();
                    comment.setCommentId(5L);
                    comment.setStatus(1);
                    return comment;
                case "insertCommentLike":
                    calls.add("insert");
                    return inserted;
                case "countCommentLikesByUser":
                    calls.add("count");
                    return liked;
                case "increaseLikeCount":
                    calls.add("increment");
                    return updated;
                case "deleteCommentLike":
                    calls.add("delete");
                    return inserted;
                case "decreaseLikeCount":
                    calls.add("decrement");
                    return updated;
                default:
                    return defaultValue(method);
            }
        });
    }

    private interface ProxyCall {
        Object invoke(String methodName, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(String method) {
        if ("toString".equals(method)) return "ProductCommentMapper proxy";
        return null;
    }
}
