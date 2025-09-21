package app.model.utilities.api;

import app.control.api.TooManyRequests;

public interface TrackSearchEndpoint {
    TrackSearchResultList search(String token, String query) throws TooManyRequests;
}
