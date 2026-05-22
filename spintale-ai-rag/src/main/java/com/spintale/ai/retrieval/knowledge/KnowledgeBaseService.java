package com.spintale.ai.retrieval.knowledge;

import java.util.List;
import java.util.Optional;
import com.spintale.ai.retrieval.document.Document;

public interface KnowledgeBaseService
{
    KnowledgeBase create(String name, String description, String embeddingModel);

    Optional<KnowledgeBase> get(String kbId);

    List<KnowledgeBase> listAll();

    List<KnowledgeBase> listByCreator(Long userId);

    void update(KnowledgeBase kb);

    void delete(String kbId);

    void activate(String kbId);

    void deactivate(String kbId);

    Document addDocument(String kbId, String name, String source, String mimeType);

    void removeDocument(String kbId, String documentId);

    List<Document> listDocuments(String kbId);

    KnowledgeBase.KnowledgeBaseStats getStats(String kbId);

    void rebuildIndex(String kbId);

    boolean exists(String kbId);

    boolean isAccessible(String kbId, Long userId);
}
