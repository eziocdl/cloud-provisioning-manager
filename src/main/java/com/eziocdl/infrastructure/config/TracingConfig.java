package com.eziocdl.infrastructure.config;

import brave.Tracing;
import brave.http.HttpTracing;
import brave.propagation.B3Propagation;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.Sampler;
import brave.jakarta.servlet.TracingFilter;
import io.micrometer.tracing.brave.bridge.BraveBaggageManager;
import io.micrometer.tracing.brave.bridge.BraveCurrentTraceContext;
import io.micrometer.tracing.brave.bridge.BraveTracer;
import jakarta.servlet.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.brave.AsyncZipkinSpanHandler;
import zipkin2.reporter.urlconnection.URLConnectionSender;

@Configuration
public class TracingConfig {

    @Value("${management.zipkin.tracing.endpoint:http://localhost:9411/api/v2/spans}")
    private String zipkinEndpoint;

    @Value("${spring.application.name:cloud-provisioning-manager}")
    private String serviceName;

    @Bean
    public BytesMessageSender sender() {
        return URLConnectionSender.create(zipkinEndpoint);
    }

    @Bean
    public AsyncZipkinSpanHandler zipkinSpanHandler(BytesMessageSender sender) {
        return AsyncZipkinSpanHandler.create(sender);
    }

    @Bean
    public ThreadLocalCurrentTraceContext braveCurrentTraceContext() {
        return ThreadLocalCurrentTraceContext.newBuilder()
                .build();
    }

    @Bean
    public Tracing tracing(AsyncZipkinSpanHandler spanHandler, ThreadLocalCurrentTraceContext currentTraceContext) {
        return Tracing.newBuilder()
                .localServiceName(serviceName)
                .currentTraceContext(currentTraceContext)
                .sampler(Sampler.ALWAYS_SAMPLE)
                .propagationFactory(B3Propagation.FACTORY)
                .addSpanHandler(spanHandler)
                .build();
    }

    @Bean
    public brave.Tracer braveTracer(Tracing tracing) {
        return tracing.tracer();
    }

    @Bean
    public BraveCurrentTraceContext bridgeContext(ThreadLocalCurrentTraceContext currentTraceContext) {
        return new BraveCurrentTraceContext(currentTraceContext);
    }

    @Bean
    public io.micrometer.tracing.Tracer micrometerTracer(brave.Tracer braveTracer, BraveCurrentTraceContext bridgeContext) {
        return new BraveTracer(braveTracer, bridgeContext, new BraveBaggageManager());
    }

    @Bean
    public HttpTracing httpTracing(Tracing tracing) {
        return HttpTracing.create(tracing);
    }

    @Bean
    public Filter tracingFilter(HttpTracing httpTracing) {
        return TracingFilter.create(httpTracing);
    }
}
