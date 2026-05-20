package com.spintale.ai.retrieval.rag;

import com.spintale.ai.api.api.ChatClient;
import com.spintale.ai.core.model.ChatMessage;
import com.spintale.ai.core.model.ChatRequest;
import com.spintale.ai.core.model.ChatResponse;
import com.spintale.ai.core.options.ChatOptions;
import com.spintale.ai.retrieval.vector.VectorStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * RAG (Retrieval-Augmented Generation) pipeline.
 * Combines vector retrieval with LLM generation for accurate, grounded responses.
 */
@Slf4j
public class RagPipeline {

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final RagConfig config;

    @Builder
    public RagPipeline(VectorStore vectorStore, 
                      ChatClient chatClient,
                      RagConfig config) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
        this.config = config != null ? config : RagConfig.defaultConfig();
    }

    /**
     * Execute RAG pipeline: retrieve + generate.
     *
     * @param query user query
     * @return RAG response with retrieved context
     */
    public RagResponse execute(String query) {
        log.info("Executing RAG pipeline for query: {}", query);
        long startTime = System.currentTimeMillis();

        // Step 1: Retrieve relevant documents
        List<EmbeddingMatch<TextSegment>> matches = vectorStore.search(
            query, 
            config.getMaxResults(),
            config.getMinScore()
        );

        // Step 2: Build context from retrieved segments
        String context = buildContext(matches);
        log.debug("Retrieved {} segments, context length: {}", matches.size(), context.length());

        // Step 3: Generate response with context
        String systemPrompt = buildSystemPrompt(context);
        
        ChatResponse response = chatClient
            .system(systemPrompt)
            .user(query)
            .options(config.getChatOptions())
            .call();

        long duration = System.currentTimeMillis() - startTime;
        log.info("RAG pipeline completed in {}ms", duration);

        return RagResponse.builder()
            .answer(response.getContent())
            .retrievedSegments(matches)
            .context(context)
            .tokenUsage(response.getTokenUsage())
            .executionTimeMs(duration)
            .build();
    }

    /**
     * Build context string from retrieved segments.
     */
    private String buildContext(List<EmbeddingMatch<TextSegment>> matches) {
        return matches.stream()
            .map(match -> match.embedded().text())
            .collect(Collectors.joining("\n\n---\n\n"));
    }

    /**
     * Build system prompt with retrieved context.
     */
    private String buildSystemPrompt(String context) {
        return String.format("""
            You are a helpful AI assistant. Use the following retrieved context to answer the user's question.
            
            Context:
            %s
            
            Instructions:
            - Base your answer ONLY on the provided context
            - If the context doesn't contain relevant information, say "I cannot find relevant information in the provided context"
            - Cite specific parts of the context when possible
            - Be concise and accurate
            
            Answer:
            """, context);
    }

    /**
     * Index documents into the vector store.
     */
    public void indexDocuments(List<dev.langchain4j.data.document.Document> documents) {
        log.info("Indexing {} documents for RAG", documents.size());
        vectorStore.indexDocuments(documents);
    }

    /**
     * Configuration for RAG pipeline.
     */
    @Data
    @Builder
    public static class RagConfig {
        @Builder.Default
        private int maxResults = 5;
        
        @Builder.Default
        private double minScore = 0.7;
        
        private ChatOptions chatOptions;

        public static RagConfig defaultConfig() {
            return RagConfig.builder()
                .maxResults(5)
                .minScore(0.7)
                .chatOptions(ChatOptions.builder().temperature(0.7).build())
                .build();
        }
    }

    /**
     * RAG response with metadata.
     */
    @Data
    @Builder
    public static class RagResponse {
        private String answer;
        private List<EmbeddingMatch<TextSegment>> retrievedSegments;
        private String context;
        private com.spintale.ai.core.model.TokenUsage tokenUsage;
        private long executionTimeMs;

        /**
         * Get number of retrieved segments.
         */
        public int getRetrievedCount() {
            return retrievedSegments != null ? retrievedSegments.size() : 0;
        }
    }
}
