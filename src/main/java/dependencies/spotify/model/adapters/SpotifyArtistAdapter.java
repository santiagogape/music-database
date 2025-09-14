package dependencies.spotify.model.adapters;

import app.model.items.Artist;
import dependencies.spotify.model.items.SpotifyArtist;


public class SpotifyArtistAdapter implements Artist {

    private final SpotifyArtist artist;

    public SpotifyArtistAdapter(SpotifyArtist artist) {
        this.artist = artist;
    }

    @Override
    public Integer id() {
        return 0;
    }

    @Override
    public String name() {
        return artist.getName();
    }

    @Override
    public ItemType type() {
        return ItemType.artist;
    }
}
