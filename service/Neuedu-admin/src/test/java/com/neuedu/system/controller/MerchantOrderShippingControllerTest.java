package com.neuedu.system.controller;

import com.neuedu.common.core.domain.model.LoginUser;
import com.neuedu.common.core.domain.entity.SysUser;
import com.neuedu.system.constants.OrderStatusConstants;
import com.neuedu.system.domain.OrderDetail;
import com.neuedu.system.domain.OrderInfo;
import com.neuedu.system.domain.Product;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IOrderInfoService;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.IShopService;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MerchantOrderShippingControllerTest {
    @After
    public void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void rejectsShippingWhenAnyOrderItemBelongsToAnotherShop() throws Exception {
        ShippingFixture fixture = new ShippingFixture(22L);

        fixture.controller.merchantShipOrder("order-1", Collections.<String, String>emptyMap());

        assertEquals(0, fixture.shipCalls);
    }

    @Test
    public void allowsShippingWhenEveryOrderItemBelongsToCurrentShop() throws Exception {
        ShippingFixture fixture = new ShippingFixture(11L);

        fixture.controller.merchantShipOrder("order-1", Collections.<String, String>emptyMap());

        assertEquals(1, fixture.shipCalls);
    }

    private static class ShippingFixture {
        private final OrderInfoController controller;
        private int shipCalls;

        private ShippingFixture(Long productShopId) throws Exception {
            SysUser user = new SysUser();
            user.setUserId(7L);
            LoginUser loginUser = new LoginUser(7L, 1L, user, Collections.emptySet());
            SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(loginUser, null, Collections.emptyList()));

            OrderDetail detail = new OrderDetail();
            detail.setProductId(101L);
            OrderInfo order = new OrderInfo();
            order.setStatus(OrderStatusConstants.WAIT_SHIP);
            order.setOrderDetails(Collections.singletonList(detail));
            IOrderInfoService orders = proxy(IOrderInfoService.class, (method, args) -> {
                if ("selectOrderInfoByOrderNo".equals(method)) return order;
                if ("shipOrder".equals(method)) {
                    shipCalls++;
                    return 1;
                }
                return null;
            });
            controller = new OrderInfoController(orders);

            Shop shop = new Shop();
            shop.setId(11L);
            inject("shopService", proxy(IShopService.class, (method, args) ->
                "selectShopByUserId".equals(method) ? shop : null));
            Product product = new Product();
            product.setShopId(productShopId);
            inject("productService", proxy(IProductService.class, (method, args) ->
                "selectProductById".equals(method) ? product : null));
        }

        private void inject(String fieldName, Object value) throws Exception {
            Field field = OrderInfoController.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(controller, value);
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
