package com.cobre.notification.infrastructure.http;

import com.cobre.notification.domain.port.out.WebhookClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Map;

@Component
public class WebhookHttpClient implements WebhookClient {

    private static final Logger logger = LoggerFactory.getLogger(WebhookHttpClient.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    private final RestTemplate restTemplate;

    public WebhookHttpClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public WebhookResponse post(String url, String payload, Map<String, String> headers) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            headers.forEach(httpHeaders::add);

            HttpEntity<String> entity = new HttpEntity<>(payload, httpHeaders);

            logger.debug("Sending webhook to: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            int statusCode = response.getStatusCode().value();
            boolean success = statusCode >= 200 && statusCode < 300;

            return new WebhookResponse(
                    statusCode,
                    response.getBody(),
                    success ? null : "HTTP " + statusCode,
                    success
            );

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.warn("HTTP error delivering webhook: {}", e.getMessage());
            return new WebhookResponse(
                    e.getStatusCode().value(),
                    e.getResponseBodyAsString(),
                    e.getMessage(),
                    false
            );
        } catch (ResourceAccessException e) {
            logger.error("Network error delivering webhook", e);
            return new WebhookResponse(
                    0,
                    null,
                    "Network error: " + e.getMessage(),
                    false
            );
        } catch (Exception e) {
            logger.error("Unexpected error delivering webhook", e);
            return new WebhookResponse(
                    0,
                    null,
                    "Unexpected error: " + e.getMessage(),
                    false
            );
        }
    }
}
