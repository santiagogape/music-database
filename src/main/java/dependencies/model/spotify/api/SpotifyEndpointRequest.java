package dependencies.model.spotify.api;

import app.control.api.TooManyRequests;
import app.model.utilities.api.EndpointRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SpotifyEndpointRequest implements EndpointRequest {
    private final HttpClient client;

    public SpotifyEndpointRequest(
            HttpClient client
    ) {
        this.client = client;
    }

    @Override
    public String requestAndGetResponseBody(String token, String requestUrl) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                System.err.println("Error: " + response.body());
                String after = response.headers().firstValue("Retry-After").orElse("unknown");
                System.err.println("Retry-After: " + after);
                throw new TooManyRequests("Retry after:"+after);
            } else if (response.statusCode() != 200) {
                throw new RuntimeException(response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Status: " + response.statusCode());
        // todo: check in case status different from 200
        return response.body();
    }
}
