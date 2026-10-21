package com.neuedu.system.service;

import com.neuedu.system.mapper.UserFootprintMapper;
import com.neuedu.system.domain.Category;
import com.neuedu.system.domain.Product;
import com.neuedu.system.domain.UserFootprint;
import com.neuedu.system.service.ICategoryService;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.impl.UserFootprintServiceImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserFootprintOwnershipTest {
    @Test
    public void deleteByIdIncludesOwnerInMapperUpdate() throws Exception {
        UserFootprintServiceImpl service = new UserFootprintServiceImpl(null, null, null, null, null);
        Long[] requested = new Long[2];
        UserFootprintMapper mapper = proxy(UserFootprintMapper.class, (method, args) -> {
            if ("deleteUserFootprintByIdForUser".equals(method)) {
                requested[0] = (Long) args[0];
                requested[1] = (Long) args[1];
                return 1;
            }
            return 0;
        });
        Field field = UserFootprintServiceImpl.class.getDeclaredField("userFootprintMapper");
        field.setAccessible(true);
        field.set(service, mapper);

        assertEquals(1, service.deleteUserFootprintByIdForUser(28L, 71L));
        assertEquals(Long.valueOf(28L), requested[0]);
        assertEquals(Long.valueOf(71L), requested[1]);
    }

    @Test
    public void recordsProductCategoryNameAlongsideFootprint() {
        Product product = new Product();
        product.setName("测试商品");
        product.setCategoryId(9L);
        Category category = new Category();
        category.setName("测试分类");
        UserFootprint[] inserted = new UserFootprint[1];

        UserFootprintMapper mapper = proxy(UserFootprintMapper.class, (method, args) -> {
            if ("selectFootprintByUserAndTarget".equals(method)) return null;
            if ("insertUserFootprint".equals(method)) {
                inserted[0] = (UserFootprint) args[0];
                return 1;
            }
            return 0;
        });
        IProductService products = proxy(IProductService.class, (method, args) ->
                "selectProductById".equals(method) ? product : null);
        ICategoryService categories = proxy(ICategoryService.class, (method, args) ->
                "selectCategoryById".equals(method) ? category : null);
        UserFootprintServiceImpl service = new UserFootprintServiceImpl(mapper, products, null, null, categories);

        assertEquals(1, service.recordFootprint(28L, 1, 71L));
        assertNotNull(inserted[0]);
        assertEquals(Long.valueOf(9L), inserted[0].getCategoryId());
        assertEquals("测试分类", inserted[0].getCategoryName());
    }

    private interface ProxyCall {
        Object invoke(String method, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
