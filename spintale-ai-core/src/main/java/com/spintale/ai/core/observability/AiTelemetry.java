package com.spintale.ai.core.observability;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AiTelemetry {
    
    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;
    private final TextMapPropagator propagator;
    
    @Value("${spintale.ai.telemetry.service-name:spintale-ai}")
    private String serviceName = "spintale-ai";
    
    @Value("${spintale.ai.telemetry.enabled:true}")
    private boolean enabled = true;
    
    public AiTelemetry() {
        this.openTelemetry = OpenTelemetry.noop();
        this.tracer = openTelemetry.getTracer(serviceName, "1.0.0");
        this.propagator = openTelemetry.getPropagators().getTextMapPropagator();
    }
    
    public AiTelemetry(OpenTelemetry openTelemetry) {
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer(serviceName, "1.0.0");
        this.propagator = openTelemetry.getPropagators().getTextMapPropagator();
    }
    
    public Tracer getTracer() {
        return tracer;
    }
    
    public OpenTelemetry getOpenTelemetry() {
        return openTelemetry;
    }
    
    public TextMapPropagator getPropagator() {
        return propagator;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public Context getCurrentContext() {
        return Context.current();
    }
}
