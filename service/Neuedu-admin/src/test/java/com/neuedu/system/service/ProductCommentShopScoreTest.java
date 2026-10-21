package com.neuedu.system.service;

import com.neuedu.system.domain.Product;
import com.neuedu.system.domain.ProductComment;
import com.neuedu.system.mapper.ProductCommentMapper;
import com.neuedu.system.service.impl.ProductCommentServiceImpl;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

public class ProductCommentShopScoreTest {
    @Test
    public void approvedRootReviewRefreshesShopScoreFromAllShopReviews() {
        ProductComment comment = new ProductComment();
        comment.setCommentId(3L);
        comment.setProductId(48L);
        comment.setParentId(0L);
        comment.setStatus(2);

        Product product = new Product();
        product.setShopId(7L);
        BigDecimal[] updatedScore = new BigDecimal[1];

        ProductCommentMapper comments = proxy(ProductCommentMapper.class, (method, args) -> {
            if ("selectProductCommentByCommentId".equals(method)) return comment;
            if ("updateCommentStatus".equals(method)) {
                comment.setStatus((Integer) args[1]);
                return 1;
            }
            if ("calculateShopAverageScore".equals(method)) return 4.25d;
            return defaultValue(method);
        });
        IProductService products = proxy(IProductService.class,
                (method, args) -> "selectProductById".equals(method) ? product : defaultValue(method));
        IShopService shops = proxy(IShopService.class, (method, args) -> {
            if ("updateShopScore".equals(method)) {
                updatedScore[0] = (BigDecimal) args[1];
                return 1;
            }
            return defaultValue(method);
        });

        ProductCommentServiceImpl service = new ProductCommentServiceImpl(comments, products, shops);
        assertEquals(true, service.auditComment(3L, 1, null));
        assertEquals(new BigDecimal("4.25"), updatedScore[0]);
    }

    @Test
    public void shopWithoutApprovedRatingsReceivesZeroScore() throws Exception {
        BigDecimal[] updatedScore = new BigDecimal[1];
        ProductCommentMapper comments = proxy(ProductCommentMapper.class,
                (method, args) -> "calculateShopAverageScore".equals(method) ? null : defaultValue(method));
        IProductService products = proxy(IProductService.class, (method, args) -> defaultValue(method));
        IShopService shops = proxy(IShopService.class, (method, args) -> {
            if ("updateShopScore".equals(method)) {
                updatedScore[0] = (BigDecimal) args[1];
                return 1;
            }
            return defaultValue(method);
        });

        ProductCommentServiceImpl service = new ProductCommentServiceImpl(comments, products, shops);
        java.lang.reflect.Method refresh = ProductCommentServiceImpl.class
                .getDeclaredMethod("updateShopAverageScore", Long.class);
        refresh.setAccessible(true);
        refresh.invoke(service, 7L);

        assertEquals(BigDecimal.ZERO, updatedScore[0]);
    }

    private interface ProxyCall {
        Object invoke(String method, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }

    private static Object defaultValue(String method) {
        return null;
    }
}
