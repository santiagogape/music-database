package app.control;

import dependencies.spotify.model.auth.AccessToken;

public interface TokenManager {
    void start();
    void end();
    AccessToken accessToken();
}
