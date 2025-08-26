package app.control;

import dependencies.spotify.model.auth.AccessToken;

public interface TokenManager {
    AccessToken requestToken();
}
