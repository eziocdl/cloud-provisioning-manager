package com.eziocdl.infrastructure.config;

import brave.Tracing;
import brave.http.HttpTracing;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder(Tracing tracing) {
        return WebClient.builder()
                .filter(tracingFilter(tracing));
    }

    private ExchangeFilterFunction tracingFilter(Tracing tracing) {
        return (request, next) -> {
            var span = tracing.tracer().currentSpan();
            if (span != null) {
                var tracedRequest = ClientRequest.from(request)
                        .header("X-B3-TraceId", span.context().traceIdString())
                        .header("X-B3-SpanId", span.context().spanIdString())
                        .header("X-B3-Sampled", "1")
                        .build();
                return next.exchange(tracedRequest);
            }
            return next.exchange(request);
        };
    }
}
