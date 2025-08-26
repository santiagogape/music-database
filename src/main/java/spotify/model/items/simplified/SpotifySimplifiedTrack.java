package spotify.model.items.simplified;

import spotify.model.items.SpotifyExternalUrls;

import java.util.List;

public class SpotifySimplifiedTrack extends SpotifySimplifiedObject {
    private final List<SpotifySimplifiedObject> artists;
    private final int track_number;

    public SpotifySimplifiedTrack(
            String id, SpotifyExternalUrls external_urls,
            String name,
            String type,
            String uri,
            List<SpotifySimplifiedObject> artists,
            int track_number) {
        super(id, external_urls, name, type, uri);
        this.artists = artists;
        this.track_number = track_number;

    }

    public List<SpotifySimplifiedObject> getArtists() {
        return artists;
    }

    public int getTrackNumber() {
        return track_number;
    }

    @Override
    public String toString() {
        return "SpotifySimplifiedTrack{" + super.toString() +
                ", artists=" + artists +
                ", trackNumber=" + track_number +
                '}';
    }
}
