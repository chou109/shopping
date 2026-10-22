package com.neuedu.system.controller;

import com.neuedu.system.service.IOrderInfoService;
import org.junit.Test;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import static org.junit.Assert.assertArrayEquals;

public class OrderInfoRouteRegistrationTest {
    @Test
    public void registersAllOrderRoutesWithoutAmbiguousMappings() throws NoSuchMethodException {
        IOrderInfoService service = (IOrderInfoService) Proxy.newProxyInstance(
            IOrderInfoService.class.getClassLoader(), new Class<?>[]{IOrderInfoService.class},
            (proxy, method, args) -> null);
        OrderInfoController controller = new OrderInfoController(service);
        MappingRegistry mappingRegistry = new MappingRegistry();

        mappingRegistry.registerController(controller);

        assertArrayEquals(new String[]{"/detail/{orderNo}"},
            OrderInfoController.class.getMethod("getOrderDetail", String.class)
                .getAnnotation(GetMapping.class).value());
    }

    private static final class MappingRegistry extends RequestMappingHandlerMapping {
        private void registerController(Object controller) {
            for (Method method : controller.getClass().getMethods()) {
                RequestMappingInfo mapping = getMappingForMethod(method, controller.getClass());
                if (mapping != null) {
                    registerMapping(mapping, controller, method);
                }
            }
        }
    }
}
