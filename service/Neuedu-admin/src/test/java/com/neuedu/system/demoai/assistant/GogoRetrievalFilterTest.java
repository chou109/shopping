package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuedu.system.demoai.assistant.prompt.GogoPromptFactory;
import com.neuedu.system.demoai.entity.Product;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.repository.ProductRepository;
import com.neuedu.system.service.IOrderInfoService;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GogoRetrievalFilterTest {
    @Test
    public void unrelatedProductsAreNotPresentedAsKnowledgeSources() {
        Product unrelated = product(7L, "手机保护壳");
        ProductRepository products = proxy(ProductRepository.class, (proxy, method, args) -> {
            if ("searchActiveByKeyword".equals(method.getName())) return Collections.singletonList(unrelated);
            return defaultValue(method.getReturnType());
        });
        GogoAssistantService assistant = assistant(products);
        AtomicReference<List<Map<String, Object>>> sources = new AtomicReference<>();

        assistant.answer("退货政策", null, null, new ArrayList<>(), token -> { }, sources::set, handoff -> { });

        assertTrue(sources.get().isEmpty());
    }

    @Test
    public void explicitlySelectedProductRemainsAvailableAsContext() {
        Product unrelated = product(7L, "手机保护壳");
        Product selected = product(9L, "当前查看的耳机");
        ProductRepository products = proxy(ProductRepository.class, (proxy, method, args) -> {
            if ("searchActiveByKeyword".equals(method.getName())) return Collections.singletonList(unrelated);
            if ("findByIdAndDeleteFlagAndStatus".equals(method.getName())) return selected;
            return defaultValue(method.getReturnType());
        });
        GogoAssistantService assistant = assistant(products);
        AtomicReference<List<Map<String, Object>>> sources = new AtomicReference<>();

        assistant.answer("退货政策", 9L, null, new ArrayList<>(), token -> { }, sources::set, handoff -> { });

        assertEquals(1, sources.get().size());
        assertTrue(String.valueOf(sources.get().get(0).get("title")).contains("当前查看的耳机"));
    }

    private GogoAssistantService assistant(ProductRepository products) {
        FAQRepository faqs = proxy(FAQRepository.class, (proxy, method, args) -> {
            if ("findForAssistant".equals(method.getName())) return Collections.emptyList();
            return defaultValue(method.getReturnType());
        });
        return new GogoAssistantService(new ObjectMapper(), faqs, products,
                proxy(IOrderInfoService.class, (proxy, method, args) -> defaultValue(method.getReturnType())),
                new GogoPromptFactory());
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0D;
        return null;
    }
}
