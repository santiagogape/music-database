package dependencies.spotify.model.items;

import dependencies.spotify.model.items.full.SpotifyTrack;

import java.util.List;

public record SpotifyTrackSearchResponse(TracksResult tracks) {

    public record TracksResult(String href, List<SpotifyTrack> items){}
}
