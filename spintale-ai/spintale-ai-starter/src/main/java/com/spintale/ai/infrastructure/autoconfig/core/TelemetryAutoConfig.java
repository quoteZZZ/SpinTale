package com.spintale.ai.infrastructure.autoconfig.core;

import com.spintale.ai.core.observability.AiTelemetry;
import com.spintale.ai.core.observability.TracingHelper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "spintale.ai.telemetry", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TelemetryAutoConfig {
    
    @Value("${spintale.ai.telemetry.service-name:spintale-ai}")
    private String serviceName;
    
    @Value("${spintale.ai.telemetry.otlp.endpoint:http://localhost:4317}")
    private String otlpEndpoint;
    
    @Bean
    @ConditionalOnMissingBean
    public OpenTelemetry openTelemetry() {
        OtlpGrpcSpanExporter spanExporter = OtlpGrpcSpanExporter.builder()
                .setEndpoint(otlpEndpoint)
                .build();
        
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .setResource(io.opentelemetry.sdk.resources.Resource.builder()
                        .put(io.opentelemetry.semconv.ResourceAttributes.SERVICE_NAME, serviceName)
                        .build())
                .build();
        
        return OpenTelemetrySdk.builder()
                .setTracerProvider(tracerProvider)
                .buildAndRegisterGlobal();
    }
    
    @Bean
    @ConditionalOnMissingBean
    public AiTelemetry aiTelemetry(OpenTelemetry openTelemetry) {
        return new AiTelemetry(openTelemetry);
    }
    
    @Bean
    @ConditionalOnMissingBean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer(serviceName, "1.0.0");
    }
    
    @Bean
    @ConditionalOnMissingBean
    public TracingHelper tracingHelper(Tracer tracer) {
        return new TracingHelper(tracer);
    }
}
