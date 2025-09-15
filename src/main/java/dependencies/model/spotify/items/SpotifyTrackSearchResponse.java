package dependencies.model.spotify.items;

import dependencies.model.spotify.items.full.SpotifyTrack;

import java.util.List;

public record SpotifyTrackSearchResponse(TracksResult tracks) {

    public record TracksResult(String href, List<SpotifyTrack> items){}
}
