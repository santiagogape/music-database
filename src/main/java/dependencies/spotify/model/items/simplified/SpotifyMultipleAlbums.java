package dependencies.spotify.model.items.simplified;

import dependencies.spotify.model.items.full.SpotifyAlbum;

import java.util.List;

public record SpotifyMultipleAlbums(List<SpotifyAlbum> albums) {
}
