package app.model.utilities.api;

import java.util.List;

public record TrackSearchResultList(String query, List<TrackSearchResult> tracks) {
    public record TrackSearchResult(
            String trackId,
            String albumId,
            List<String> albumArtistsIds,
            List<String> artistsId,
            String trackUrl,
            String albumUrl,
            List<String> albumArtistsUrls,
            List<String> artistsUrl,
            String trackName,
            int trackNumber
    ) {
    }
}
