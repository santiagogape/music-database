package dependencies.spotify.control.search;

import app.control.api.TooManyRequests;
import app.control.api.TrackSearch;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class SpotifyTrackSearch implements TrackSearch {

    private final HttpClient client;

    public SpotifyTrackSearch(HttpClient client) {
        this.client = client;
    }

    @Override
    public String search(String token, String query) throws TooManyRequests {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithQuery(query)))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 429) {
                System.out.println("Error: " + response.body());
                String after = response.headers().firstValue("Retry-After").orElse("unknown");
                System.out.println("Retry-After: " + after);
                throw new TooManyRequests("Retry after:"+after);
            } else if (response.statusCode() != 200) {
                throw new RuntimeException(response.body());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Status: " + response.statusCode());
        // todo: check in case status different from 200
        System.out.println("Response body:");
        System.out.println(response.body());
        return response.body();
    }

    private static String urlWithQuery(String query) {
        return String.format(
                "https://api.spotify.com/v1/search?q=%s&type=track&limit=10",
                URLEncoder.encode(query, StandardCharsets.UTF_8)
        );
    }
}
