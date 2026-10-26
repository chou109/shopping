package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neuedu.system.demoai.assistant.prompt.GogoPromptFactory;
import com.neuedu.system.demoai.repository.FAQRepository;
import com.neuedu.system.demoai.repository.ProductRepository;
import com.neuedu.system.service.IOrderInfoService;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GogoAssistantStreamingTest {
    @Test
    public void streamsServerSentModelTokensAndStoresOnlyCompletedTurn() throws Exception {
        HttpServer server = server(200, "data: {\"choices\":[{\"delta\":{\"content\":\"你好\"}}]}\n\n"
                + "data: {\"choices\":[],\"usage\":{\"prompt_tokens\":3,\"completion_tokens\":1}}\n\n"
                + "data: [DONE]\n\n");
        try {
            GogoAssistantService assistant = assistant(server);
            List<String> tokens = new ArrayList<>();
            AtomicBoolean handoff = new AtomicBoolean(true);
            List<com.fasterxml.jackson.databind.node.ObjectNode> history = new ArrayList<>();

            assistant.answer("你好", null, null, history, tokens::add, sources -> { }, handoff::set);

            assertEquals(Collections.singletonList("你好"), tokens);
            assertFalse(handoff.get());
            assertEquals(2, history.size());
            assertEquals("assistant", history.get(1).path("role").asText());
            assertEquals("你好", history.get(1).path("content").asText());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void upstreamHttpFailureReturnsSafeHandoffMessage() throws Exception {
        HttpServer server = server(503, "{\"error\":\"private upstream detail\"}");
        try {
            GogoAssistantService assistant = assistant(server);
            List<String> tokens = new ArrayList<>();
            AtomicBoolean handoff = new AtomicBoolean(false);

            assistant.answer("你好", null, null, new ArrayList<>(), tokens::add, sources -> { }, handoff::set);

            assertEquals(1, tokens.size());
            assertFalse(tokens.get(0).contains("private upstream detail"));
            assertTrue(handoff.get());
        } finally {
            server.stop(0);
        }
    }

    private GogoAssistantService assistant(HttpServer server) throws Exception {
        GogoAssistantService assistant = new GogoAssistantService(new ObjectMapper(),
                proxy(FAQRepository.class), proxy(ProductRepository.class), proxy(IOrderInfoService.class), new GogoPromptFactory());
        set(assistant, "apiKey", "unit-test-key");
        set(assistant, "baseUrl", "http://127.0.0.1:" + server.getAddress().getPort());
        set(assistant, "readTimeout", 2000);
        return assistant;
    }

    private HttpServer server(int status, String body) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/chat/completions", exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", status == 200 ? "text/event-stream" : "application/json");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) { output.write(bytes); }
        });
        server.start();
        return server;
    }

    @SuppressWarnings("unchecked")
    private <T> T proxy(Class<T> type) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> {
                    if ("findForAssistant".equals(method.getName()) || "searchActiveByKeyword".equals(method.getName())) return Collections.emptyList();
                    if (method.getReturnType().isPrimitive()) {
                        if (method.getReturnType() == boolean.class) return false;
                        if (method.getReturnType() == int.class) return 0;
                        if (method.getReturnType() == long.class) return 0L;
                    }
                    return null;
                });
    }

    private void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
