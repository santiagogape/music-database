package dependencies.spotify.model.items.simplified;

import app.model.items.ImageRef;
import dependencies.spotify.model.items.SpotifyExternalUrls;

import java.util.List;

public class SpotifySimplifiedAlbum extends SpotifySimplifiedObject {
    private final String release_date_precision;
    private final String album_type;
    private final List<ImageRef> images;
    private final String release_date;
    private final int total_tracks;
    private final List<SpotifySimplifiedObject> artists;

    public SpotifySimplifiedAlbum(
            String id, SpotifyExternalUrls external_urls,
            String name,
            String type,
            String uri,
            String album_type, // album, single, compilation
            int total_tracks,
            List<ImageRef> images,
            String release_date, // f:year-mont-day
            String release_date_precision, // year, month or day
            List<SpotifySimplifiedObject> artists){
            super(id, external_urls, name, type, uri);
            this.album_type = album_type;
            this.images = images;
            this.release_date = release_date;
            this.release_date_precision = release_date_precision;
            this.total_tracks = total_tracks;
            this.artists = artists;
    }

    @Override
    public String toString() {
        return "SpotifySimplifiedAlbum{" + super.toString() +
                ", releasePrecision='" + release_date_precision + '\'' +
                ", albumType='" + album_type + '\'' +
                ", release='" + release_date + '\'' +
                ", total tracks='" + total_tracks + '\'' +
                ", artists='" + artists + '\'' +
                '}';
    }

    public String getReleasePrecision() {
        return release_date_precision;
    }

    public String getAlbumType() {
        return album_type;
    }

    public List<ImageRef> getImages() {
        return images;
    }

    public String getRelease() {
        return release_date;
    }
}
