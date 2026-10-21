package com.neuedu.system.service;

import com.neuedu.common.exception.ServiceException;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.mapper.ShopMapper;
import com.neuedu.system.service.impl.ShopServiceImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ShopServiceTest {
    @Test
    public void rejectsShopCreationWhenOwnerDoesNotExist() throws Exception {
        ShopFixture fixture = new ShopFixture(null);
        Shop shop = new Shop();
        shop.setUserId(404L);

        try {
            fixture.service.insertShop(shop);
            fail("Expected missing shop owner to be rejected");
        } catch (ServiceException error) {
            assertEquals("店铺所有者不存在，请选择有效的系统用户", error.getMessage());
        }

        assertEquals(0, fixture.insertCalls);
    }

    @Test
    public void genericShopUpdateCannotChangeSystemOwnedFields() throws Exception {
        ShopFixture fixture = new ShopFixture(null);
        Shop shop = new Shop();
        shop.setId(7L);
        shop.setUserId(99L);
        shop.setStatus(2);
        shop.setDeleteFlag(1);
        shop.setShopScore(new java.math.BigDecimal("5.0"));
        shop.setSalesCount(100L);
        shop.setProductCount(100L);
        shop.setFollowerCount(100L);
        shop.setShopName("允许更新的资料");

        assertEquals(1, fixture.service.updateShop(shop));

        assertEquals(1, fixture.updateCalls);
        assertNull(fixture.updatedShop.getUserId());
        assertNull(fixture.updatedShop.getStatus());
        assertNull(fixture.updatedShop.getDeleteFlag());
        assertNull(fixture.updatedShop.getShopScore());
        assertNull(fixture.updatedShop.getSalesCount());
        assertNull(fixture.updatedShop.getProductCount());
        assertNull(fixture.updatedShop.getFollowerCount());
        assertEquals("允许更新的资料", fixture.updatedShop.getShopName());
    }

    @Test
    public void looksUpShopUsingItsOwnerUserId() throws Exception {
        ShopFixture fixture = new ShopFixture(null);

        Shop shop = fixture.service.selectShopByUserId(27L);

        assertEquals(Long.valueOf(27L), fixture.queriedOwnerUserId);
        assertEquals(Long.valueOf(27L), shop.getUserId());
    }

    private static class ShopFixture {
        private final ShopServiceImpl service;
        private int insertCalls;
        private int updateCalls;
        private Shop updatedShop;
        private Long queriedOwnerUserId;

        private ShopFixture(Object owner) throws Exception {
            ShopMapper mapper = proxy(ShopMapper.class, (methodName, args) -> {
                if ("insertShop".equals(methodName)) {
                    insertCalls++;
                    return 1;
                }
                if ("updateShop".equals(methodName)) {
                    updateCalls++;
                    updatedShop = (Shop) args[0];
                    return 1;
                }
                if ("selectShopByUserId".equals(methodName)) {
                    queriedOwnerUserId = (Long) args[0];
                    Shop shop = new Shop();
                    shop.setUserId(queriedOwnerUserId);
                    return shop;
                }
                return null;
            });
            service = new ShopServiceImpl(mapper);
            ISysUserService users = proxy(ISysUserService.class, (methodName, args) ->
                "selectUserById".equals(methodName) ? owner : null);
            Field field = ShopServiceImpl.class.getDeclaredField("sysUserService");
            field.setAccessible(true);
            field.set(service, users);
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
