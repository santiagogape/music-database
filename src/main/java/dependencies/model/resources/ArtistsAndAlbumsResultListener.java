package dependencies.model.resources;

import app.model.items.Album;
import app.model.items.Artist;
import app.model.items.SimpleItem;

import java.util.List;
import java.util.Map;

public interface ArtistsAndAlbumsResultListener {
    void setArtists(List<Artist> artists);

    void setAlbums(List<Album> albums);

    void setArtistURIs(List<SimpleItem.ItemUri> uris);

    void setAlbumURIs(List<SimpleItem.ItemUri> uris);

    SearchResult get();

    record SearchResult(Map<Integer, Artist> artists, Map<Integer, Album> albums,
                        Map<String, SimpleItem.ItemUri> artistsUris, Map<String, SimpleItem.ItemUri> albumsUris) {
    }
}
