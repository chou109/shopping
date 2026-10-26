package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuedu.system.demoai.assistant.prompt.GogoPromptFactory;
import com.neuedu.system.demoai.entity.FrequentlyAskedQuestion;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.repository.ProductRepository;
import com.neuedu.system.service.IOrderInfoService;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertTrue;

/** Small deterministic smoke evaluation; results are not a substitute for a labeled production corpus. */
public class GogoRetrievalEvaluationTest {
    @Test
    public void fixedQueriesMeetRecallMrrAndNoAnswerThresholds() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode fixture;
        try (InputStream input = getClass().getResourceAsStream("/gogo-rag-evaluation.json")) {
            if (input == null) throw new IllegalStateException("missing_gogo_rag_evaluation_fixture");
            fixture = mapper.readTree(input);
        }
        List<FrequentlyAskedQuestion> corpus = new ArrayList<>();
        for (JsonNode item : fixture.path("corpus")) {
            FrequentlyAskedQuestion faq = new FrequentlyAskedQuestion();
            faq.setId(item.path("id").asLong());
            faq.setQuestion(item.path("question").asText());
            faq.setAnswer(item.path("answer").asText());
            faq.setSourceType("FAQ");
            corpus.add(faq);
        }
        FAQRepository faqs = proxy(FAQRepository.class, (proxy, method, args) ->
                "findForAssistant".equals(method.getName()) ? corpus : defaultValue(method.getReturnType()));
        ProductRepository products = proxy(ProductRepository.class, (proxy, method, args) ->
                "searchActiveByKeyword".equals(method.getName()) ? Collections.emptyList() : defaultValue(method.getReturnType()));
        GogoAssistantService assistant = new GogoAssistantService(mapper, faqs, products,
                proxy(IOrderInfoService.class, (proxy, method, args) -> defaultValue(method.getReturnType())), new GogoPromptFactory());

        int positiveCases = 0;
        int recalledAtFive = 0;
        int noAnswerCases = 0;
        int correctlyAbstained = 0;
        double reciprocalRankTotal = 0D;
        for (JsonNode testCase : fixture.path("cases")) {
            String query = testCase.path("query").asText();
            JsonNode expectedNode = testCase.get("expectedSourceId");
            AtomicReference<List<Map<String, Object>>> result = new AtomicReference<>(Collections.emptyList());
            assistant.answer(query, null, null, new ArrayList<>(), token -> { }, result::set, handoff -> { });
            if (expectedNode == null || expectedNode.isNull()) {
                noAnswerCases++;
                if (result.get().isEmpty()) correctlyAbstained++;
                continue;
            }
            positiveCases++;
            Long expectedId = expectedNode.asLong();
            int rank = 0;
            for (Map<String, Object> source : result.get()) {
                rank++;
                if (expectedId.equals(source.get("sourceId"))) break;
            }
            if (rank > 0 && rank <= 5 && rank <= result.get().size()
                    && expectedId.equals(result.get().get(rank - 1).get("sourceId"))) {
                recalledAtFive++;
                reciprocalRankTotal += 1D / rank;
            }
        }
        double recallAtFive = positiveCases == 0 ? 0D : (double) recalledAtFive / positiveCases;
        double mrr = positiveCases == 0 ? 0D : reciprocalRankTotal / positiveCases;
        double noAnswerAccuracy = noAnswerCases == 0 ? 0D : (double) correctlyAbstained / noAnswerCases;
        assertTrue("synthetic Recall@5=" + recallAtFive, recallAtFive >= 0.75D);
        assertTrue("synthetic MRR=" + mrr, mrr >= 0.6D);
        assertTrue("synthetic no-answer accuracy=" + noAnswerAccuracy, noAnswerAccuracy == 1D);
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type, java.lang.reflect.InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == double.class) return 0D;
        return null;
    }
}
