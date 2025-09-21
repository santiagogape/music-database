package dependencies.model.resources;

import app.model.items.FileSong;
import app.model.items.Response;

public record FileSongResponseTrackSearchResult(FileSong fileSong, Response response, app.model.utilities.api.TrackSearchResultList.TrackSearchResult trackSearchResult) {
}
