package dependencies.model.spotify.api;

import app.control.api.TooManyRequests;
import app.control.files.json.JsonConverter;
import app.model.utilities.api.EndpointRequest;
import app.model.utilities.api.TrackSearchEndpoint;
import app.model.utilities.api.TrackSearchResultList;
import dependencies.model.spotify.adapters.SpotifyTrackSearchResultAdapter;
import dependencies.model.spotify.items.SpotifyTrackSearchResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SpotifyTrackSearchEndpoint implements TrackSearchEndpoint {

    private static String endpointUrlWith(String query) {
        return String.format(
                "https://api.spotify.com/v1/search?q=%s&type=track&limit=10",
                URLEncoder.encode(query, StandardCharsets.UTF_8)
        );
    }

    private final JsonConverter converter;
    private final EndpointRequest endpointRequest;

    public SpotifyTrackSearchEndpoint(
            JsonConverter converter,
            EndpointRequest endpointRequest
    ) {
        this.converter = converter;
        this.endpointRequest = endpointRequest;
    }

    @Override
    public TrackSearchResultList search(String token, String query) throws TooManyRequests {
        String body = endpointRequest.requestAndGetResponseBody(token, endpointUrlWith(query));
        return new TrackSearchResultList(query, toListOfSpotifyTrackSearchResult(converter.fromJson(body, SpotifyTrackSearchResponse.class)));
    }

    private List<TrackSearchResultList.TrackSearchResult> toListOfSpotifyTrackSearchResult(SpotifyTrackSearchResponse result) {
        return result.tracks().items().stream().map(SpotifyTrackSearchResultAdapter::from).toList();

    }


}
