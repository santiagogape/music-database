package dependencies.spotify.control;

import com.google.gson.Gson;
import dependencies.spotify.model.auth.AccessToken;
import dependencies.spotify.model.auth.TokenRequest;
import dependencies.spotify.model.auth.TokenResponse;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;

public class TokenManager implements app.control.TokenManager {
    private final TokenRequest solicitude;
    private final HttpClient client;

    public TokenManager(HttpClient client, TokenRequest solicitude) {
        this.solicitude = solicitude;
        this.client = client;
    }

    @Override
    public AccessToken requestToken() {
        try {
            HttpResponse<String> response = this.client.send(this.solicitude.request(), HttpResponse.BodyHandlers.ofString());
            TokenResponse token = new Gson().fromJson(response.body(), TokenResponse.class);
            return new AccessToken(token.access_token(), token.token_type());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
