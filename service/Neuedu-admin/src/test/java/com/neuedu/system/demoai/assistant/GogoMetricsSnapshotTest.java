package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuedu.system.demoai.assistant.prompt.GogoPromptFactory;
import com.neuedu.system.demoai.repository.AIChatMetricEventRepository;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.repository.ProductRepository;
import com.neuedu.system.service.IOrderInfoService;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class GogoMetricsSnapshotTest {
    @Test
    public void countsSafeModelFallbackAsHumanHandoff() {
        GogoAssistantService assistant = new GogoAssistantService(new ObjectMapper(), proxy(FAQRepository.class, empty()),
                proxy(ProductRepository.class, empty()), proxy(IOrderInfoService.class, empty()), new GogoPromptFactory());

        assistant.answer("咨询商品", null, null, new ArrayList<>(), token -> { }, sources -> { }, handoff -> { });

        assertEquals(1D, (Double) assistant.metricsSnapshot().get("handoffRate"), 0D);
    }

    @Test
    public void buildsTheAdminSnapshotFromPersistedThirtyDayTotals() {
        AIChatMetricEventRepository metrics = proxy(AIChatMetricEventRepository.class, (instance, method, args) ->
                "summarizeSince".equals(method.getName())
                        ? new Object[]{2L, 1L, 4L, 1L, 300L, 100L, 50L, new BigDecimal("0.12500000"), 1L}
                        : defaultValue(method.getReturnType()));
        GogoAssistantService assistant = new GogoAssistantService(new ObjectMapper(), proxy(FAQRepository.class, empty()),
                proxy(ProductRepository.class, empty()), proxy(IOrderInfoService.class, empty()), new GogoPromptFactory(), null, metrics);

        Map<String, Object> snapshot = assistant.metricsSnapshot();

        assertEquals(2L, snapshot.get("requests"));
        assertEquals(0.5D, (Double) snapshot.get("retrievalHitRate"), 0.0001D);
        assertEquals(0.75D, (Double) snapshot.get("toolSuccessRate"), 0.0001D);
        assertEquals(0.5D, (Double) snapshot.get("handoffRate"), 0.0001D);
        assertEquals(150D, (Double) snapshot.get("averageResponseMs"), 0.0001D);
        assertEquals(30, snapshot.get("periodDays"));
        assertEquals(new BigDecimal("0.12500000"), snapshot.get("estimatedCost"));
    }

    private InvocationHandler empty() { return (instance, method, args) -> defaultValue(method.getReturnType()); }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler);
    }

    private static Object defaultValue(Class<?> type) {
        if (!type.isPrimitive()) return null;
        if (type == boolean.class) return false;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        return null;
    }
}
