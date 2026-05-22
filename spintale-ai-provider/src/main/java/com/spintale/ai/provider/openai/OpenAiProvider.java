package com.spintale.ai.provider.openai;

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
public class OpenAiProvider
{
    private String providerId = "openai";
    private String baseUrl = "https://api.openai.com/v1";
    private String apiKey;
    private Duration timeout = Duration.ofSeconds(60);
    private Duration connectTimeout = Duration.ofSeconds(10);
    private WebClient webClient;

    public void init()
    {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public OpenAiChatResponse chat(OpenAiChatRequest request)
    {
        if (webClient == null) init();

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiChatResponse.class)
                .timeout(timeout)
                .block();
    }

    public Mono<OpenAiChatResponse> chatAsync(OpenAiChatRequest request)
    {
        if (webClient == null) init();

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiChatResponse.class)
                .timeout(timeout);
    }

    public Flux<String> chatStream(OpenAiChatRequest request)
    {
        if (webClient == null) init();

        OpenAiChatRequest streamRequest = OpenAiChatRequest.builder()
                .model(request.getModel())
                .messages(request.getMessages())
                .temperature(request.getTemperature())
                .maxTokens(request.getMaxTokens())
                .tools(request.getTools())
                .stream(true)
                .build();

        return webClient.post()
                .uri("/chat/completions")
                .bodyValue(streamRequest)
                .retrieve()
                .bodyToFlux(String.class)
                .timeout(timeout);
    }

    public List<String> listModels()
    {
        if (webClient == null) init();

        Map<String, Object> response = webClient.get()
                .uri("/models")
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(timeout)
                .block();

        if (response == null || !response.containsKey("data"))
        {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> models = (List<Map<String, Object>>) response.get("data");
        return models.stream()
                .map(m -> (String) m.get("id"))
                .toList();
    }

    public OpenAiEmbeddingResponse embed(OpenAiEmbeddingRequest request)
    {
        if (webClient == null) init();

        return webClient.post()
                .uri("/embeddings")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(OpenAiEmbeddingResponse.class)
                .timeout(timeout)
                .block();
    }

    public static OpenAiProvider create(String apiKey)
    {
        OpenAiProvider provider = OpenAiProvider.builder()
                .apiKey(apiKey)
                .build();
        provider.init();
        return provider;
    }

    public static OpenAiProvider create(String baseUrl, String apiKey)
    {
        OpenAiProvider provider = OpenAiProvider.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .build();
        provider.init();
        return provider;
    }
}
