package dependencies.spotify.model.adapters;

import app.model.items.Album;
import dependencies.spotify.model.items.full.SpotifyAlbum;

import java.time.LocalDateTime;

public class SpotifyAlbumAdapter implements Album {
    private final SpotifyAlbum album;

    public SpotifyAlbumAdapter(SpotifyAlbum album) {
        this.album = album;
    }

    @Override
    public AlbumType albumType() {
        return AlbumType.album;
    }

    @Override
    public int tracks() {
        return album.getTotal_tracks();
    }

    @Override
    public LocalDateTime release() {
        return Album.ReleasePrecision.toLocalDateTime(precision(), album.getRelease());
    }

    @Override
    public ReleasePrecision precision() {
        return ReleasePrecision.valueOf(album.getReleasePrecision());
    }

    @Override
    public String label() {
        return album.getLabel();
    }

    @Override
    public Integer id() {
        return 0;
    }

    @Override
    public String name() {
        return album.getName();
    }

    @Override
    public ItemType type() {
        return ItemType.album;
    }
}
