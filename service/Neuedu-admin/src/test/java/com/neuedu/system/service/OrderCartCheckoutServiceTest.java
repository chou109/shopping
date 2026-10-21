package com.neuedu.system.service;

import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.system.domain.Cart;
import com.neuedu.system.domain.Product;
import com.neuedu.system.domain.UserAddress;
import com.neuedu.system.mapper.OrderInfoMapper;
import com.neuedu.system.service.impl.OrderInfoServiceImpl;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class OrderCartCheckoutServiceTest {
    @Test
    public void rejectsMixedStoreCartBeforeCreatingOrderOrChangingInventory() throws Exception {
        CheckoutFixture fixture = new CheckoutFixture();

        try {
            fixture.service.createOrderFromCart(7L, 9L);
            fail("Expected cross-store checkout to be rejected");
        } catch (RuntimeException error) {
            assertEquals("购物车所选商品来自多个店铺，请分店选择商品后结算", error.getMessage());
        }

        assertEquals(0, fixture.orderInserts);
        assertEquals(0, fixture.stockUpdates);
        assertEquals(0, fixture.cartClears);
    }

    private static class CheckoutFixture {
        private final OrderInfoServiceImpl service = new OrderInfoServiceImpl();
        private final Map<Long, Product> products = new HashMap<>();
        private int orderInserts;
        private int stockUpdates;
        private int cartClears;

        private CheckoutFixture() throws Exception {
            products.put(101L, product(101L, 1L));
            products.put(202L, product(202L, 2L));

            Cart first = cart(101L);
            Cart second = cart(202L);
            List<Cart> selectedItems = Arrays.asList(first, second);

            UserAddress address = new UserAddress();
            address.setId(9L);
            address.setCreateUser(7L);

            inject("sysUserService", proxy(ISysUserService.class, (method, args) ->
                "selectUserById".equals(method) ? new SysUser() : null));
            inject("userAddressService", proxy(IUserAddressService.class, (method, args) ->
                "selectUserAddressById".equals(method) ? address : null));
            inject("cartService", proxy(ICartService.class, (method, args) -> {
                if ("getSelectedCartItems".equals(method)) return selectedItems;
                if ("clearSelectedCartItems".equals(method)) {
                    cartClears++;
                    return 2;
                }
                return null;
            }));
            inject("productService", proxy(IProductService.class, (method, args) -> {
                if ("selectProductById".equals(method)) return products.get(args[0]);
                if ("updateProductStock".equals(method)) stockUpdates++;
                return 1;
            }));
            inject("orderInfoMapper", proxy(OrderInfoMapper.class, (method, args) -> {
                if ("insertOrderInfo".equals(method)) orderInserts++;
                return 1;
            }));
        }

        private static Product product(Long id, Long shopId) {
            Product product = new Product();
            product.setId(id);
            product.setName("商品" + id);
            product.setStatus(1);
            product.setStock(10L);
            product.setPrice(new BigDecimal("10.00"));
            product.setShopId(shopId);
            product.setIsBlindBox(0);
            return product;
        }

        private static Cart cart(Long productId) {
            Cart cart = new Cart();
            cart.setProductId(productId);
            cart.setProductName("商品" + productId);
            cart.setQuantity(1);
            return cart;
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
