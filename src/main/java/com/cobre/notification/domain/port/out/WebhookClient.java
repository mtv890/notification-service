package com.cobre.notification.domain.port.out;

import java.util.Map;

public interface WebhookClient {
    WebhookResponse post(String url, String payload, Map<String, String> headers);

    class WebhookResponse {
        private final int statusCode;
        private final String responseBody;
        private final String errorMessage;
        private final boolean success;

        public WebhookResponse(int statusCode, String responseBody, String errorMessage, boolean success) {
            this.statusCode = statusCode;
            this.responseBody = responseBody;
            this.errorMessage = errorMessage;
            this.success = success;
        }

        public int getStatusCode() { return statusCode; }
        public String getResponseBody() { return responseBody; }
        public String getErrorMessage() { return errorMessage; }
        public boolean isSuccess() { return success; }
    }
}