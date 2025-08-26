package spotify.model.items;

import app.model.items.ImageRef;
import spotify.model.items.simplified.SpotifySimplifiedObject;

import java.util.List;

public class SpotifyArtist extends SpotifySimplifiedObject{

    private final List<String> genres;
    private final List<ImageRef> images;

    public SpotifyArtist(
            String id, SpotifyExternalUrls external_urls,
            String name,
            String type,
            String uri,
            List<String> genres,
            List<ImageRef> images
) {
        super(id, external_urls, name, type, uri);
        this.genres = genres;
        this.images = images;
    }

    public List<String> getGenres() {
        return genres;
    }

    public List<ImageRef> getImages() {
        return images;
    }
}
