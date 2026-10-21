package com.neuedu.system.controller;

import com.neuedu.common.core.domain.AjaxResult;
import com.neuedu.system.domain.Product;
import com.neuedu.system.domain.Shop;
import com.neuedu.system.service.IProductService;
import com.neuedu.system.service.IShopService;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ProductPublicControllerTest {
    @Test
    public void publicDetailsReturnActiveProducts() {
        Product product = new Product();
        product.setStatus(1);
        ProductController controller = controllerFor(product);

        AjaxResult result = controller.getPublicInfo(8L);

        assertEquals(200, result.get("code"));
        assertEquals(product, result.get("data"));
    }

    @Test
    public void publicDetailsRejectInactiveProducts() {
        Product product = new Product();
        product.setStatus(0);

        AjaxResult result = controllerFor(product).getPublicInfo(8L);

        assertNotEquals(200, result.get("code"));
    }

    @Test
    public void publicDetailsRejectProductsFromClosedShops() {
        Product product = new Product();
        product.setStatus(1);
        product.setShopId(12L);
        Shop closedShop = new Shop();
        closedShop.setId(12L);
        closedShop.setStatus(2);
        ProductController controller = controllerFor(product, closedShop);

        AjaxResult result = controller.getPublicInfo(8L);

        assertNotEquals(200, result.get("code"));
    }

    @Test
    public void shopProductListRejectsClosedShops() {
        Shop closedShop = new Shop();
        closedShop.setId(12L);
        closedShop.setStatus(2);

        AjaxResult result = controllerFor(null, closedShop).byShop(12L);

        assertNotEquals(200, result.get("code"));
    }

    @Test
    public void merchantProductQueryAlwaysUsesTheirOwnShop() {
        Shop ownShop = new Shop();
        ownShop.setId(12L);
        Product query = new Product();
        query.setShopId(99L);
        ProductController controller = controllerFor(null, ownShop);

        controller.scopeProductQueryToCurrentShop(query, 71L);

        assertEquals(Long.valueOf(12L), query.getShopId());
    }

    @Test
    public void merchantWithoutShopCannotQueryAllProducts() {
        Product query = new Product();
        ProductController controller = controllerFor(null, null);

        controller.scopeProductQueryToCurrentShop(query, 71L);

        assertEquals(Long.valueOf(-1L), query.getShopId());
    }

    private ProductController controllerFor(Product product) {
        return controllerFor(product, null);
    }

    private ProductController controllerFor(Product product, Shop shop) {
        IProductService products = proxy(IProductService.class, (methodName, args) ->
            "selectProductById".equals(methodName) ? product : null);
        IShopService shops = proxy(IShopService.class, (methodName, args) ->
            "selectShopById".equals(methodName) || "selectShopByUserId".equals(methodName) ? shop : null);
        return new ProductController(products, shops);
    }

    private interface ProxyCall {
        Object invoke(String methodName, Object[] args);
    }

    private static <T> T proxy(Class<T> type, ProxyCall call) {
        InvocationHandler handler = (instance, method, args) -> call.invoke(method.getName(), args);
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
