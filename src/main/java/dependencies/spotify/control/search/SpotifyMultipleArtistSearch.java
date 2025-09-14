package dependencies.spotify.control.search;

import app.control.api.MultipleSearcher;
import app.control.api.TooManyRequests;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SpotifyMultipleArtistSearch implements MultipleSearcher {

    private final HttpClient client;

    public SpotifyMultipleArtistSearch(HttpClient client) {
        this.client = client;
    }

    @Override
    public String search(String token, List<String> ids) {
        if (ids.size() >= 50) throw new RuntimeException("too many artist id:"+ids.size());
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithArtistIds(ids)))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        HttpResponse<String> response;
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

    private static String urlWithArtistIds(List<String> ids) {
        return String.format(
                "https://api.spotify.com/v1/artists?ids=%s",
                URLEncoder.encode(String.join(",",ids), StandardCharsets.UTF_8)
        );
    }
}
