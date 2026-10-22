package com.neuedu.system.service;

import com.neuedu.system.mapper.ProductMapper;
import com.neuedu.system.service.impl.ProductServiceImpl;
import org.junit.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProductStockAtomicUpdateTest {
    @Test
    public void insufficientConcurrentDecrementIsRejectedWhenAtomicUpdateAffectsNoRow() {
        AtomicInteger stockReads = new AtomicInteger();
        ProductMapper mapper = mapperReturning(0, stockReads);
        ProductServiceImpl service = new ProductServiceImpl(mapper);

        try {
            service.updateProductStock(9L, -2L);
            fail("库存未满足原子扣减条件时应返回错误");
        } catch (RuntimeException exception) {
            assertEquals("库存不足或商品不存在", exception.getMessage());
        }
        assertEquals("扣减前不应再单独读取易过期的库存", 0, stockReads.get());
    }

    @Test
    public void successfulAtomicDecrementReturnsUpdatedRowCount() {
        ProductServiceImpl service = new ProductServiceImpl(mapperReturning(1, new AtomicInteger()));

        assertEquals(1, service.updateProductStock(9L, -2L));
    }

    @Test
    public void nullStockMutationArgumentsAreRejected() {
        ProductServiceImpl service = new ProductServiceImpl(mapperReturning(1, new AtomicInteger()));

        try {
            service.updateProductStock(9L, null);
            fail("库存变更数量不能为空");
        } catch (RuntimeException exception) {
            assertEquals("商品和库存变更数量不能为空", exception.getMessage());
        }
    }

    private ProductMapper mapperReturning(int updateResult, AtomicInteger stockReads) {
        return (ProductMapper) Proxy.newProxyInstance(ProductMapper.class.getClassLoader(),
                new Class<?>[]{ProductMapper.class}, (proxy, method, args) -> {
                    if ("selectProductById".equals(method.getName())) {
                        stockReads.incrementAndGet();
                    }
                    if ("updateProductStock".equals(method.getName())) {
                        return updateResult;
                    }
                    return null;
                });
    }
}
