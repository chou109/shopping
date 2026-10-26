package com.neuedu.system.demoai.assistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/** OpenAI-compatible embeddings client, configured independently from the chat model. */
@Slf4j
@Component
public class OpenAiCompatibleEmbeddingProvider implements EmbeddingProvider {
    private final ObjectMapper mapper;

    @Value("${ai.gogo.embedding.api-key:}") private String apiKey;
    @Value("${ai.gogo.embedding.base-url:}") private String baseUrl;
    @Value("${ai.gogo.embedding.model:}") private String model;
    @Value("${ai.gogo.embedding.connect-timeout-ms:3000}") private int connectTimeout;
    @Value("${ai.gogo.embedding.read-timeout-ms:10000}") private int readTimeout;

    public OpenAiCompatibleEmbeddingProvider(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean isConfigured() {
        return hasText(apiKey) && hasText(baseUrl) && hasText(model);
    }

    @Override
    public String cacheNamespace() {
        try { return URI.create(baseUrl).getHost() + ":" + model; }
        catch (Exception ex) { return model == null ? "embedding-model" : model; }
    }

    @Override
    public List<float[]> embed(List<String> texts) throws Exception {
        if (!isConfigured() || texts == null || texts.isEmpty()) return new ArrayList<>();
        HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl.replaceAll("/$", "") + "/embeddings").openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(Math.max(500, Math.min(connectTimeout, 15000)));
        connection.setReadTimeout(Math.max(1000, Math.min(readTimeout, 30000)));
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");
        try {
            com.fasterxml.jackson.databind.node.ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.set("input", mapper.valueToTree(texts));
            try (OutputStream output = connection.getOutputStream()) { output.write(mapper.writeValueAsBytes(body)); }
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                InputStream error = connection.getErrorStream();
                if (error != null) error.close();
                throw new IllegalStateException("embedding_http_" + status);
            }
            JsonNode root = mapper.readTree(connection.getInputStream());
            JsonNode data = root.path("data");
            if (!data.isArray() || data.size() != texts.size()) throw new IllegalStateException("embedding_response_count_mismatch");
            float[][] ordered = new float[texts.size()][];
            int dimensions = -1;
            for (JsonNode row : data) {
                JsonNode indexNode = row.path("index");
                if (!indexNode.isIntegralNumber() || indexNode.asInt() < 0 || indexNode.asInt() >= texts.size()
                        || ordered[indexNode.asInt()] != null) throw new IllegalStateException("embedding_index_invalid");
                JsonNode embedding = row.path("embedding");
                if (!embedding.isArray() || embedding.isEmpty()) throw new IllegalStateException("embedding_vector_missing");
                if (dimensions < 0) dimensions = embedding.size();
                if (embedding.size() != dimensions) throw new IllegalStateException("embedding_dimension_mismatch");
                float[] vector = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    JsonNode value = embedding.get(i);
                    if (!value.isNumber() || !Double.isFinite(value.asDouble())) throw new IllegalStateException("embedding_value_invalid");
                    vector[i] = (float) value.asDouble();
                    if (!Float.isFinite(vector[i])) throw new IllegalStateException("embedding_value_invalid");
                }
                ordered[indexNode.asInt()] = vector;
            }
            List<float[]> vectors = new ArrayList<>();
            for (float[] vector : ordered) {
                if (vector == null) throw new IllegalStateException("embedding_index_missing");
                vectors.add(vector);
            }
            return vectors;
        } finally {
            connection.disconnect();
        }
    }

    private boolean hasText(String value) { return value != null && !value.trim().isEmpty(); }
}
