package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.lang.reflect.Field;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OpenAiCompatibleEmbeddingProviderTest {
    @Test
    public void sendsAuthenticatedBatchAndRestoresVectorsByResponseIndex() throws Exception {
        AtomicReference<String> authorization = new AtomicReference<>();
        AtomicReference<String> requestBody = new AtomicReference<>();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/embeddings", exchange -> {
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            requestBody.set(new String(readAll(exchange.getRequestBody()), StandardCharsets.UTF_8));
            byte[] body = "{\"data\":[{\"index\":1,\"embedding\":[0,1]},{\"index\":0,\"embedding\":[1,0]}]}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            OpenAiCompatibleEmbeddingProvider provider = new OpenAiCompatibleEmbeddingProvider(new ObjectMapper());
            set(provider, "apiKey", "embedding-test-secret");
            set(provider, "baseUrl", "http://127.0.0.1:" + server.getAddress().getPort() + "/v1");
            set(provider, "model", "test-embedding-model");
            List<float[]> result = provider.embed(Arrays.asList("question", "knowledge"));

            assertTrue(provider.isConfigured());
            assertEquals("Bearer embedding-test-secret", authorization.get());
            assertTrue(requestBody.get().contains("test-embedding-model"));
            assertTrue(requestBody.get().contains("question"));
            assertEquals(1F, result.get(0)[0], 0F);
            assertEquals(1F, result.get(1)[1], 0F);
            assertEquals("127.0.0.1:test-embedding-model", provider.cacheNamespace());
        } finally {
            server.stop(0);
        }
    }

    private void set(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private byte[] readAll(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        return output.toByteArray();
    }
}
