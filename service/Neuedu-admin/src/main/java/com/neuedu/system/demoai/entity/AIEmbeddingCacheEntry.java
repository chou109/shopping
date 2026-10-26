package com.neuedu.system.demoai.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ai_embedding_cache")
public class AIEmbeddingCacheEntry {
    @Id
    @Column(name = "cache_key", length = 64, nullable = false)
    private String cacheKey;

    @Column(name = "model_key", length = 255, nullable = false)
    private String modelKey;

    @Column(name = "content_hash", length = 64, nullable = false)
    private String contentHash;

    @Column(name = "vector_json", nullable = false, columnDefinition = "LONGTEXT")
    private String vectorJson;

    @Column(name = "dimensions", nullable = false)
    private Integer dimensions;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
