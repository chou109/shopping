package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuedu.system.demoai.assistant.prompt.GogoPromptFactory;
import com.neuedu.system.demoai.entity.Product;
import com.neuedu.system.demoai.entity.FrequentlyAskedQuestion;
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
        assertEquals("product", sources.get().get(0).get("sourceType"));
        assertEquals(9L, sources.get().get(0).get("sourceId"));
    }

    @Test
    public void unrelatedFaqWithOnlyOneCoincidentalBigramIsNotReturned() {
        FrequentlyAskedQuestion faq = faq(21L, "政策支持" , "本店提供日常售后服务");
        FAQRepository faqs = faqRepository(faq);
        AtomicReference<List<Map<String, Object>>> sources = new AtomicReference<>();

        assistant(faqs, emptyProducts()).answer("退款政策如何申请", null, null, new ArrayList<>(), token -> { }, sources::set, handoff -> { });

        assertTrue(sources.get().isEmpty());
    }

    @Test
    public void faqWithEnoughQuestionTermOverlapIsReturnedAsItsSourceType() {
        FrequentlyAskedQuestion faq = faq(22L, "如何申请退款", "请从订单页面提交售后申请");
        faq.setSourceType("POLICY");
        AtomicReference<List<Map<String, Object>>> sources = new AtomicReference<>();

        assistant(faqRepository(faq), emptyProducts()).answer("退款政策如何申请", null, null, new ArrayList<>(), token -> { }, sources::set, handoff -> { });

        assertEquals(1, sources.get().size());
        assertEquals("policy", sources.get().get(0).get("sourceType"));
    }

    private GogoAssistantService assistant(ProductRepository products) {
        return assistant(faqRepository(), products);
    }

    private GogoAssistantService assistant(FAQRepository faqs, ProductRepository products) {
        return new GogoAssistantService(new ObjectMapper(), faqs, products,
                proxy(IOrderInfoService.class, (proxy, method, args) -> defaultValue(method.getReturnType())),
                new GogoPromptFactory());
    }

    private FAQRepository faqRepository(FrequentlyAskedQuestion... rows) {
        List<FrequentlyAskedQuestion> data = rows.length == 0 ? Collections.emptyList() : java.util.Arrays.asList(rows);
        return proxy(FAQRepository.class, (proxy, method, args) -> {
            if ("findForAssistant".equals(method.getName())) return data;
            return defaultValue(method.getReturnType());
        });
    }

    private ProductRepository emptyProducts() {
        return proxy(ProductRepository.class, (proxy, method, args) -> defaultValue(method.getReturnType()));
    }

    private FrequentlyAskedQuestion faq(Long id, String question, String answer) {
        FrequentlyAskedQuestion faq = new FrequentlyAskedQuestion();
        faq.setId(id);
        faq.setQuestion(question);
        faq.setAnswer(answer);
        faq.setSourceType("FAQ");
        return faq;
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
