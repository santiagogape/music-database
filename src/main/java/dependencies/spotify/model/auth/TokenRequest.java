package dependencies.spotify.model.auth;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.util.Base64;

public class TokenRequest {

    private final String auth;

    public TokenRequest(ClientCredentials credentials) {
       this.auth = Base64.getEncoder().encodeToString((credentials.client_id() + ":" + credentials.client_secret()).getBytes());
    }

    public HttpRequest request(){
        try {
            return HttpRequest.newBuilder()
                    .uri(new URI("https://accounts.spotify.com/api/token"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .header("Authorization", "Basic " + auth)
                    .POST(HttpRequest.BodyPublishers.ofString("grant_type=client_credentials"))
                    .build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
