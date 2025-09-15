package dependencies.model.resources;

import app.model.items.FileSong;
import app.model.items.Response;
import dependencies.model.spotify.items.full.SpotifyTrack;

public record FileSongResponseSpotifyTrack(FileSong fileSong, Response response, SpotifyTrack spotifyTrack) {
}
