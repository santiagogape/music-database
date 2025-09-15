package dependencies.model.resources;

import app.model.items.FileSong;
import app.model.items.Response;
import dependencies.model.spotify.items.SpotifyTrackSearchResponse;

public record FileSongResponseSpotifyTrackSearchResponse(FileSong fileSong, Response response,
                                                         SpotifyTrackSearchResponse result) {
}
