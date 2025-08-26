package Main;

import app.control.TokenManager;
import app.model.utilities.ChronometerListener;
import com.google.gson.Gson;
import spotify.model.auth.AccessToken;
import spotify.model.auth.ClientCredentials;
import spotify.model.auth.TokenRequest;
import spotify.model.items.full.SpotifyTrack;
import spotify.model.items.simplified.SpotifySimplifiedAlbum;
import spotify.model.items.simplified.SpotifySimplifiedObject;

import java.io.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        Gson gson = new Gson();
        InputStreamReader reader = getInputStreamReader("/client-credentials.json");
        ClientCredentials credentials = gson.fromJson(reader, ClientCredentials.class);
        System.out.println(credentials);

        SpotifySimplifiedObject simple = gson.fromJson(getInputStreamReader("/examples/simple.json"), SpotifySimplifiedObject.class);
        SpotifySimplifiedAlbum simplifiedAlbum = gson.fromJson(getInputStreamReader("/examples/simplealbum.json"), SpotifySimplifiedAlbum.class);
        SpotifyTrack track = gson.fromJson(getInputStreamReader("/examples/track.json"), SpotifyTrack.class);
        System.out.println(simple);
        System.out.println(simplifiedAlbum);
        System.out.println(track);


        TokenManager tokenManager = new spotify.control.TokenManager(HttpClient.newHttpClient(), new TokenRequest(credentials));

        /*
        AccessToken token = tokenManager.requestToken();
        token.getChronometer().registerListener(checkChronometer());
        //TODO: control of chronometer an refresh token to then update so called accessToken for the one who calls api
         */


    }

    private static InputStreamReader getInputStreamReader(String path) {
        return new InputStreamReader(
                Objects.requireNonNull(
                        Main.class.getResourceAsStream(path)
                ),
                StandardCharsets.UTF_8);
    }

    private static ChronometerListener checkChronometer() {
        return new ChronometerListener() {
            @Override
            public void OnFinish() {
                refreshToken();
            }
            @Override
            public void OnTick() {
                updateTimeLeft();
            }
        };
    }

    private static void updateTimeLeft() {
    }

    private static void refreshToken(){}
}
