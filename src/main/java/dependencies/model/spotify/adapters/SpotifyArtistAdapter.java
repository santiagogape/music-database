package dependencies.model.spotify.adapters;

import app.model.items.Artist;
import dependencies.model.spotify.items.SpotifyArtist;


public class SpotifyArtistAdapter implements Artist {


    private final String name;

    public SpotifyArtistAdapter(SpotifyArtist artist) {
        this.name = artist.getName();
    }

    @Override
    public Integer id() {
        return 0;
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public ItemType type() {
        return ItemType.artist;
    }
}
