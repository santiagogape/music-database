package dependencies.model.spotify.items.simplified;

import dependencies.model.spotify.items.full.SpotifyAlbum;

import java.util.List;

public record SpotifyMultipleAlbums(List<SpotifyAlbum> albums) {
}
