package spotify.model.items.full;

import spotify.model.items.SpotifyExternalUrls;
import spotify.model.items.simplified.SpotifySimplifiedAlbum;
import spotify.model.items.simplified.SpotifySimplifiedObject;
import spotify.model.items.simplified.SpotifySimplifiedTrack;

import java.util.List;

public class SpotifyTrack extends SpotifySimplifiedTrack {
    private final SpotifySimplifiedAlbum album;

    public  SpotifyTrack(
            String id, SpotifyExternalUrls external_urls,
            String name,
            String type,
            String uri,
            List<SpotifySimplifiedObject> artists,
            int track_number,
            SpotifySimplifiedAlbum album
)  {
        super(id, external_urls, name, type, uri, artists, track_number);
        this.album = album;
    }

    public SpotifySimplifiedAlbum getAlbum() {
        return album;
    }

    @Override
    public String toString() {
        return "SpotifyTrack{" + super.toString() +
                ", album=" + album +
                '}';
    }
}
