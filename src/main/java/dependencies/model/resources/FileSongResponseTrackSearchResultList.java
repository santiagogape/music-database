package dependencies.model.resources;

import app.model.items.FileSong;
import app.model.items.Response;
import app.model.utilities.api.TrackSearchResultList;

public record FileSongResponseTrackSearchResultList(FileSong fileSong, Response response,
                                                    TrackSearchResultList result) {
}
