package app.model.utilities.api;

public interface EndpointRequest {
    String requestAndGetResponseBody(String token, String requestUrl);
}
