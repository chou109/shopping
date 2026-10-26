package com.neuedu.system.demoai.repository;

import com.neuedu.system.demoai.entity.AIEmbeddingCacheEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AIEmbeddingCacheRepository extends JpaRepository<AIEmbeddingCacheEntry, String> {
    List<AIEmbeddingCacheEntry> findByCacheKeyIn(Collection<String> cacheKeys);
}
