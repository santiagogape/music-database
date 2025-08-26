package spotify.model.items.full;

import app.model.items.ImageRef;
import spotify.model.items.SpotifyExternalUrls;
import spotify.model.items.simplified.SpotifySimplifiedAlbum;
import spotify.model.items.simplified.SpotifySimplifiedObject;
import spotify.model.items.simplified.SpotifySimplifiedTrack;

import java.util.List;

public class SpotifyAlbum extends SpotifySimplifiedAlbum {
    private final Tracks tracks;
    private final List<String> genres;
    private final String label;

    public SpotifyAlbum(
            String id,
        SpotifyExternalUrls external_urls,
        String name,
        String type,
        String uri,
        String album_type,
        int total_tracks,
        List<ImageRef> images,
        String release_date,
        String release_dat_precision,
        List<SpotifySimplifiedObject> artists,
        Tracks tracks,
        List<String> genres,
        String label
) {
        super(id, external_urls, name, type,uri, album_type, total_tracks, images, release_date, release_dat_precision, artists);
        this.tracks = tracks;
        this.genres = genres;
        this.label = label;
    }

    public record Tracks(int total, List<SpotifySimplifiedTrack> items){}

    public Tracks getTracks() {
        return tracks;
    }

    public List<String> getGenres() {
        return genres;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return "SpotifyAlbum{" + super.toString() +
                ", tracks=" + tracks +
                ", genres=" + genres +
                ", label='" + label + '\'' +
                '}';
    }
}
