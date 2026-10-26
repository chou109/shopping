package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuedu.system.demoai.assistant.prompt.GogoPromptFactory;
import com.neuedu.system.demoai.entity.Product;
import com.neuedu.system.demoai.entity.FrequentlyAskedQuestion;
import com.neuedu.system.demoai.entity.AIEmbeddingCacheEntry;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.repository.AIEmbeddingCacheRepository;
import com.neuedu.system.demoai.repository.ProductRepository;
import com.neuedu.system.service.IOrderInfoService;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
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

    @Test
    public void offlineRewriteCasesRecallAfterSaleAndShippingFaqs() {
        FrequentlyAskedQuestion afterSale = faq(31L, "售后：退货与退款", "订单中可提交退货退款申请。");
        FrequentlyAskedQuestion delivery = faq(32L, "配送：付款后多久发货", "订单发货后可以查看物流和快递进度。");
        AtomicReference<List<Map<String, Object>>> sources = new AtomicReference<>();
        GogoAssistantService assistant = assistant(faqRepository(afterSale, delivery), emptyProducts());

        assistant.answer("请问怎么退换货品", null, null, new ArrayList<>(), token -> { }, sources::set, handoff -> { });
        assertTrue("paraphrased return question should cite its FAQ", sources.get().stream()
                .anyMatch(source -> Long.valueOf(31L).equals(source.get("sourceId"))));

        assistant.answer("付款后什么时候能查快递", null, null, new ArrayList<>(), token -> { }, sources::set, handoff -> { });
        assertTrue("paraphrased shipping question should cite its FAQ", sources.get().stream()
                .anyMatch(source -> Long.valueOf(32L).equals(source.get("sourceId"))));
    }

    @Test
    public void titleMatchRanksTheDirectAnswerAboveBroadPolicyMatches() {
        FrequentlyAskedQuestion direct = faq(41L, "如何申请退款", "请在订单页提交退款申请。");
        FrequentlyAskedQuestion broad = faq(42L, "售后服务说明", "如有问题请联系售后客服处理退款。");
        AtomicReference<List<Map<String, Object>>> sources = new AtomicReference<>();

        assistant(faqRepository(direct, broad), emptyProducts()).answer("如何申请退款", null, null,
                new ArrayList<>(), token -> { }, sources::set, handoff -> { });

        assertEquals(Long.valueOf(41L), sources.get().get(0).get("sourceId"));
    }

    @Test
    public void retrievalCapsPromptSourcesAtFiveRankedItems() {
        FrequentlyAskedQuestion[] rows = new FrequentlyAskedQuestion[8];
        for (int i = 0; i < rows.length; i++) rows[i] = faq(50L + i, "如何申请退款 " + i, "请在订单页提交退款申请。");
        AtomicReference<List<Map<String, Object>>> sources = new AtomicReference<>();

        assistant(faqRepository(rows), emptyProducts()).answer("如何申请退款", null, null,
                new ArrayList<>(), token -> { }, sources::set, handoff -> { });

        assertEquals(5, sources.get().size());
    }

    @Test
    public void semanticEmbeddingRecallsParaphraseWithoutLexicalOverlap() {
        FrequentlyAskedQuestion faq = faq(61L, "配送进度如何查询", "登录订单页查看物流信息。");
        EmbeddingProvider embeddings = embeddingProvider(text -> text.startsWith("包裹到哪") ? new float[]{1F, 0F} : new float[]{0.9F, 0.1F});
        AtomicReference<List<Map<String, Object>>> sources = new AtomicReference<>();

        assistant(faqRepository(faq), emptyProducts(), embeddings).answer("包裹到哪了", null, null,
                new ArrayList<>(), token -> { }, sources::set, handoff -> { });

        assertEquals(1, sources.get().size());
        assertEquals(Long.valueOf(61L), sources.get().get(0).get("sourceId"));
    }

    @Test
    public void lowSemanticSimilarityDoesNotAddUnrelatedKnowledge() {
        FrequentlyAskedQuestion faq = faq(62L, "配送进度如何查询", "登录订单页查看物流信息。");
        EmbeddingProvider embeddings = embeddingProvider(text -> text.startsWith("显卡") ? new float[]{1F, 0F} : new float[]{0F, 1F});
        AtomicReference<List<Map<String, Object>>> sources = new AtomicReference<>();

        assistant(faqRepository(faq), emptyProducts(), embeddings).answer("显卡支持光追吗", null, null,
                new ArrayList<>(), token -> { }, sources::set, handoff -> { });

        assertTrue(sources.get().isEmpty());
    }

    @Test
    public void persistsKnowledgeVectorsButKeepsUserQuestionVectorsProcessLocal() {
        FrequentlyAskedQuestion faq = faq(63L, "配送进度如何查询", "登录订单页查看物流信息。");
        List<AIEmbeddingCacheEntry> persisted = new ArrayList<>();
        AIEmbeddingCacheRepository cache = proxy(AIEmbeddingCacheRepository.class, (proxy, method, args) -> {
            if ("findByCacheKeyIn".equals(method.getName())) {
                java.util.Collection<?> requested = (java.util.Collection<?>) args[0];
                List<AIEmbeddingCacheEntry> matches = new ArrayList<>();
                for (AIEmbeddingCacheEntry entry : persisted) if (requested.contains(entry.getCacheKey())) matches.add(entry);
                return matches;
            }
            if ("saveAll".equals(method.getName())) {
                for (Object entry : (Iterable<?>) args[0]) persisted.add((AIEmbeddingCacheEntry) entry);
                return persisted;
            }
            return defaultValue(method.getReturnType());
        });
        AtomicReference<Integer> secondProviderInputs = new AtomicReference<>(0);
        EmbeddingProvider firstProvider = embeddingProvider(text -> text.startsWith("包裹到哪") ? new float[]{1F, 0F} : new float[]{0.9F, 0.1F});
        AtomicReference<List<Map<String, Object>>> sources = new AtomicReference<>();
        assistant(faqRepository(faq), emptyProducts(), firstProvider, cache)
                .answer("包裹到哪了", null, null, new ArrayList<>(), token -> { }, sources::set, handoff -> { });
        assertEquals(1, persisted.size());

        EmbeddingProvider secondProvider = new EmbeddingProvider() {
            @Override public boolean isConfigured() { return true; }
            @Override public String cacheNamespace() { return "test-embedding-model"; }
            @Override public List<float[]> embed(List<String> texts) {
                secondProviderInputs.set(texts.size());
                List<float[]> vectors = new ArrayList<>();
                for (String text : texts) vectors.add(text.startsWith("包裹到哪") ? new float[]{1F, 0F} : new float[]{0.9F, 0.1F});
                return vectors;
            }
        };
        assistant(faqRepository(faq), emptyProducts(), secondProvider, cache)
                .answer("包裹到哪了", null, null, new ArrayList<>(), token -> { }, sources::set, handoff -> { });

        assertEquals("second process should only embed the new query, not the cached knowledge row", Integer.valueOf(1), secondProviderInputs.get());
        assertTrue(sources.get().stream().anyMatch(source -> Long.valueOf(63L).equals(source.get("sourceId"))));
    }

    @Test
    public void productSemanticVectorExcludesLivePriceAndStock() {
        Product selected = product(64L, "轻便耳机");
        selected.setSubTitle("适合通勤");
        selected.setPrice(new java.math.BigDecimal("129.90"));
        selected.setStock(8);
        ProductRepository products = proxy(ProductRepository.class, (proxy, method, args) -> {
            if ("findByIdAndDeleteFlagAndStatus".equals(method.getName())) return selected;
            if ("searchActiveByKeyword".equals(method.getName())) return Collections.emptyList();
            return defaultValue(method.getReturnType());
        });
        List<String> embeddedTexts = new ArrayList<>();
        EmbeddingProvider embeddings = new EmbeddingProvider() {
            @Override public boolean isConfigured() { return true; }
            @Override public List<float[]> embed(List<String> texts) {
                embeddedTexts.addAll(texts);
                List<float[]> result = new ArrayList<>();
                for (int i = 0; i < texts.size(); i++) result.add(new float[]{1F, 0F});
                return result;
            }
        };

        assistant(faqRepository(), products, embeddings).answer("这款耳机是什么风格", 64L, null,
                new ArrayList<>(), token -> { }, sources -> { }, handoff -> { });

        assertTrue(embeddedTexts.stream().anyMatch(text -> text.contains("轻便耳机") && text.contains("适合通勤")));
        assertTrue(embeddedTexts.stream().noneMatch(text -> text.contains("129.90") || text.contains("库存：8")));
    }

    private GogoAssistantService assistant(ProductRepository products) {
        return assistant(faqRepository(), products);
    }

    private GogoAssistantService assistant(FAQRepository faqs, ProductRepository products) {
        return assistant(faqs, products, null);
    }

    private GogoAssistantService assistant(FAQRepository faqs, ProductRepository products, EmbeddingProvider embeddings) {
        return new GogoAssistantService(new ObjectMapper(), faqs, products,
                proxy(IOrderInfoService.class, (proxy, method, args) -> defaultValue(method.getReturnType())),
                new GogoPromptFactory(), embeddings);
    }

    private GogoAssistantService assistant(FAQRepository faqs, ProductRepository products, EmbeddingProvider embeddings,
                                           AIEmbeddingCacheRepository cache) {
        return new GogoAssistantService(new ObjectMapper(), faqs, products,
                proxy(IOrderInfoService.class, (proxy, method, args) -> defaultValue(method.getReturnType())),
                new GogoPromptFactory(), embeddings, null, cache);
    }

    private EmbeddingProvider embeddingProvider(java.util.function.Function<String, float[]> vectorFactory) {
        return new EmbeddingProvider() {
            @Override public boolean isConfigured() { return true; }
            @Override public String cacheNamespace() { return "test-embedding-model"; }
            @Override public List<float[]> embed(List<String> texts) {
                List<float[]> vectors = new ArrayList<>();
                for (String text : texts) vectors.add(vectorFactory.apply(text));
                return vectors;
            }
        };
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
