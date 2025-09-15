package app.control;

import dependencies.model.spotify.auth.AccessToken;

public interface TokenManager {
    void start();
    void end();
    AccessToken accessToken();
}
