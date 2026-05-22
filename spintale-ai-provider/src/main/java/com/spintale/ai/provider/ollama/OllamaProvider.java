package com.spintale.ai.provider.ollama;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OllamaProvider
{
    private String providerId = "ollama";
    private String baseUrl = "http://localhost:11434";
    private Duration timeout = Duration.ofSeconds(120);
    private Duration connectTimeout = Duration.ofSeconds(10);
    private WebClient webClient;

    public void init()
    {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public OllamaChatResponse chat(OllamaChatRequest request)
    {
        if (webClient == null) init();

        return webClient.post()
                .uri("/api/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaChatResponse.class)
                .timeout(timeout)
                .block();
    }

    public Mono<OllamaChatResponse> chatAsync(OllamaChatRequest request)
    {
        if (webClient == null) init();

        return webClient.post()
                .uri("/api/chat")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaChatResponse.class)
                .timeout(timeout);
    }

    public Flux<String> chatStream(OllamaChatRequest request)
    {
        if (webClient == null) init();

        OllamaChatRequest streamRequest = OllamaChatRequest.builder()
                .model(request.getModel())
                .messages(request.getMessages())
                .options(request.getOptions())
                .stream(true)
                .build();

        return webClient.post()
                .uri("/api/chat")
                .bodyValue(streamRequest)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(timeout);
    }

    public OllamaEmbeddingResponse embed(OllamaEmbeddingRequest request)
    {
        if (webClient == null) init();

        return webClient.post()
                .uri("/api/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OllamaEmbeddingResponse.class)
                .timeout(timeout)
                .block();
    }

    public List<OllamaModel> listModels()
    {
        if (webClient == null) init();

        Map<String, Object> response = webClient.get()
                .uri("/api/tags")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(timeout)
                .block();

        if (response == null || !response.containsKey("models"))
        {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("models");
        return models.stream()
                .map(m -> OllamaModel.builder()
                        .name((String) m.get("name"))
                        .modifiedAt((String) m.get("modified_at"))
                        .size((Long) m.get("size"))
                        .build())
                .toList();
    }

    public boolean pullModel(String modelName)
    {
        if (webClient == null) init();

        try
        {
            Map<String, String> request = Map.of("name", modelName);
            webClient.post()
                    .uri("/api/pull")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMinutes(10))
                    .block();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public static OllamaProvider create()
    {
        OllamaProvider provider = OllamaProvider.builder().build();
        provider.init();
        return provider;
    }

    public static OllamaProvider create(String baseUrl)
    {
        OllamaProvider provider = OllamaProvider.builder()
                .baseUrl(baseUrl)
                .build();
        provider.init();
        return provider;
    }

    @Data
    @Builder
    public static class OllamaModel
    {
        private String name;
        private String modifiedAt;
        private Long size;
    }
}
