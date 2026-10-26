package com.neuedu.system.demoai.assistant;

import java.util.List;

/** Optional semantic embedding service. Implementations return one vector per input in the same order. */
public interface EmbeddingProvider {
    boolean isConfigured();

    List<float[]> embed(List<String> texts) throws Exception;

    /** Stable, non-secret identifier used to keep incompatible model vectors separate. */
    default String cacheNamespace() { return getClass().getName(); }
}
