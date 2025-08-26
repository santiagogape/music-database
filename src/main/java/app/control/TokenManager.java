package app.control;

import spotify.model.auth.AccessToken;

public interface TokenManager {
    AccessToken requestToken();
}
