package com.spintale.ai.core.observability;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

public class TracingHelper {
    
    private static final Logger log = LoggerFactory.getLogger(TracingHelper.class);
    
    private final Tracer tracer;
    
    public TracingHelper(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public <T> T trace(String spanName, SpanKind kind, Supplier<T> operation) {
        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(kind)
                .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            T result = operation.get();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    public void trace(String spanName, SpanKind kind, Runnable operation) {
        Span span = tracer.spanBuilder(spanName)
                .setSpanKind(kind)
                .startSpan();
        
        try (Scope scope = span.makeCurrent()) {
            operation.run();
            span.setStatus(StatusCode.OK);
        } catch (Exception e) {
            span.setStatus(StatusCode.ERROR, e.getMessage());
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }
    
    public Span startSpan(String name, SpanKind kind) {
        return tracer.spanBuilder(name)
                .setSpanKind(kind)
                .startSpan();
    }
    
    public Span startSpan(String name, SpanKind kind, Context parentContext) {
        return tracer.spanBuilder(name)
                .setSpanKind(kind)
                .setParent(parentContext)
                .startSpan();
    }
    
    public void endSpan(Span span) {
        if (span != null) {
            span.end();
        }
    }
    
    public void endSpan(Span span, Throwable error) {
        if (span != null) {
            if (error != null) {
                span.setStatus(StatusCode.ERROR, error.getMessage());
                span.recordException(error);
            }
            span.end();
        }
    }
    
    public void addEvent(Span span, String name) {
        if (span != null) {
            span.addEvent(name);
        }
    }
    
    public void setAttribute(Span span, String key, String value) {
        if (span != null) {
            span.setAttribute(key, value);
        }
    }
    
    public void setAttribute(Span span, String key, long value) {
        if (span != null) {
            span.setAttribute(key, value);
        }
    }
    
    public void setAttribute(Span span, String key, double value) {
        if (span != null) {
            span.setAttribute(key, value);
        }
    }
}
