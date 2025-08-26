package Main;

import app.control.TokenManager;
import app.model.utilities.ChronometerListener;
import app.model.utilities.database.Database;
import com.google.gson.Gson;
import dependencies.spotify.model.auth.ClientCredentials;
import dependencies.spotify.model.auth.TokenRequest;
import dependencies.spotify.model.items.full.SpotifyTrack;
import dependencies.spotify.model.items.simplified.SpotifySimplifiedAlbum;
import dependencies.spotify.model.items.simplified.SpotifySimplifiedObject;

import java.io.*;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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


        TokenManager tokenManager = new dependencies.spotify.control.TokenManager(HttpClient.newHttpClient(), new TokenRequest(credentials));

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

    private static Database database(){
        return new Database() {
            String url = "jdbc:sqlite:C:\\Users\\santi\\Desktop\\music.db";

            @Override
            public String url() {
                return url;
            }

            @Override
            public void url(String url) {
                this.url = url;
            }

            Connection connection;
            @Override
            public void open() {
                try {
                    connection = DriverManager.getConnection(url);
                    try (Statement st = connection.createStatement()) {
                        st.execute("PRAGMA foreign_keys = ON;");
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void commit() {
                try {
                    connection.commit();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void close() {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
