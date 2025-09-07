package dependencies.spotify.control;

import com.google.gson.Gson;
import dependencies.spotify.model.auth.AccessToken;
import dependencies.spotify.model.auth.TokenRequest;
import dependencies.spotify.model.auth.TokenResponse;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class SpotifyTokenManager implements app.control.TokenManager {
    private final TokenRequest solicitude;
    private final HttpClient client;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private AccessToken token;
    private ScheduledFuture<?> currentTask;

    public SpotifyTokenManager(HttpClient client, TokenRequest solicitude) {
        this.solicitude = solicitude;
        this.client = client;
    }

    @Override
    public void start() {
        requestAndSchedule();
    }

    private void requestAndSchedule() {
        try {
            if (this.token != null) accessToken().getChronometer().finish();
            HttpResponse<String> response = this.client.send(this.solicitude.request(), HttpResponse.BodyHandlers.ofString());
            // todo: check error or response status before continuing
            TokenResponse token = new Gson().fromJson(response.body(), TokenResponse.class);
            AccessToken accessToken = new AccessToken(token.access_token(), token.token_type());
            System.out.println("new token: " + accessToken.token());
            this.token = accessToken;
            // a token expires in 1h, so i request 1min before it expires
            currentTask = scheduler.schedule(this::requestAndSchedule, 59, TimeUnit.MINUTES);
        } catch (Exception e) {
            System.out.println("error requesting token, trying in 5s");
            scheduler.schedule(this::requestAndSchedule, 5, TimeUnit.SECONDS);
        }
    }

    @Override
    public AccessToken accessToken() {
        return token;
    }

    @Override
    public void end() {
        if (currentTask != null) {
            currentTask.cancel(true);
        }
        this.token.getChronometer().finish();
        scheduler.shutdownNow();
    }
}
