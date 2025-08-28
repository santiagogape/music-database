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
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {


    public static void main(String[] args) {

        /*
        AccessToken token = tokenManager.requestToken();
        token.getChronometer().registerListener(checkChronometer());
        //TODO: control of chronometer an refresh token to then update so called accessToken for the one who calls api
         */


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
