package main;

import app.control.TokenManager;
import app.control.api.BatchTrackSearch;
import app.control.api.ResponsesObserver;
import app.control.files.actors.JsonWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dependencies.control.GsonJsonWriter;
import dependencies.spotify.control.SpotifyTokenManager;
import dependencies.spotify.control.search.SpotifyTrackSearch;
import dependencies.spotify.model.auth.AccessToken;
import dependencies.spotify.model.auth.ClientCredentials;
import dependencies.spotify.model.auth.TokenRequest;
import dependencies.spotify.model.auth.TokenResponse;
import dependencies.spotify.model.items.SpotifyTrackSearchResponse;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

import static java.lang.Thread.sleep;

public class mainTest {

    static final HttpClient client = HttpClient.newHttpClient();
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static final ClientCredentials credentials =
            fromJsonResource("/client-credentials.json", ClientCredentials.class);
    static final TokenManager tokenManager =
            new SpotifyTokenManager(client, new TokenRequest(credentials));

    static final JsonWriter jsonWriter = new GsonJsonWriter(gson);


    record Strings(List<String> list){}
    static Strings strings = fromJsonResource("/temp/db.original-formated.json", Strings.class);


    public static void main(String[] args) {


        //redirectSOut()
        tokenManager.start();
        while (tokenManager.accessToken() == null) {
            System.out.println("not yet");
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        AccessToken accessToken = tokenManager.accessToken();
        jsonWriter.save(new TokenResponse(accessToken.token(),"bearer","3600"), "C:\\Users\\santi\\Desktop\\desarrollo\\music-app\\music-database-v1\\src\\main\\resources\\examples\\accessToken.json");
        BatchTrackSearch batchTrackSearch = new BatchTrackSearch(strings.list(), tokenManager, 20, new SpotifyTrackSearch(client), createObserver());
        batchTrackSearch.start();
    }

    private static void redirectSOut() {
        try {
            PrintStream fileOut = new PrintStream(new FileOutputStream("output.log", true));
            System.setOut(fileOut);
            System.setErr(fileOut);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private static ResponsesObserver createObserver() {
        return new ResponsesObserver() {
            int i = 0;
            @Override
            public void notify(String query, String responseBody) {
                System.out.println("notified");
                SpotifyTrackSearchResponse tracksResult = gson.fromJson(responseBody, SpotifyTrackSearchResponse.class);
                System.out.println("as object:" + tracksResult);
                String s = jsonPathFrom(i+" query");
                i++;
                System.out.println("path: " + s);
                jsonWriter.save(tracksResult, s);
                System.out.println("written");
            }

            @Override
            public void finished() {
                tokenManager.end();
            }
        };
    }

    private static String jsonPathFrom(String query) {
        return "C:\\Users\\santi\\Desktop\\desarrollo\\music-app\\music-database-v1\\src\\main\\resources\\examples\\api-responses\\"+query.replaceAll("[\\\\/:*?\"<>|]", " ")+".json";
    }

    private static void requestingToken() {
        tokenManager.start();
        AccessToken token = tokenManager.accessToken();
        jsonWriter.save(new TokenResponse(token.token(), token.type(), "3600"),"C:\\Users\\santi\\Desktop\\desarrollo\\music-app\\music-database-v1\\src\\main\\resources\\examples\\accessToken.json");
        System.out.println(token.getChronometer().getEnd());
        tokenManager.end();
    }

    private static InputStreamReader getInputStreamReader(String path) {
        return new InputStreamReader(
                Objects.requireNonNull(
                        mainTest.class.getResourceAsStream(path)
                ),
                StandardCharsets.UTF_8);
    }

    private static <T> T fromJsonResource(String path, Class<T> tClass){
        return gson.fromJson(getInputStreamReader(path), tClass);
    }
}

