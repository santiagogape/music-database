package Main;

import Main.Database.MainDatabase;
import Main.configurations.UserConfig;
import app.control.TokenManager;
import app.control.files.JsonWriter;
import app.control.files.MP3Processor;
import app.control.files.ProcessMetadataToRequestString;
import app.control.files.TempFolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dependencies.control.GsonJsonWriter;
import dependencies.control.JAudioTaggerMP3MetadataReader;
import dependencies.model.SQLite.MusicSQLiteDatabase;
import dependencies.spotify.model.auth.ClientCredentials;
import dependencies.spotify.model.auth.TokenRequest;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class App extends Application {

    static final String DBName = "music.db";
    static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    static final ClientCredentials credentials =
            gson.fromJson(getInputStreamReader("/client-credentials.json"), ClientCredentials.class);
    static final TokenManager tokenManager =
            new dependencies.spotify.control.TokenManager(HttpClient.newHttpClient(), new TokenRequest(credentials));

    static final JsonWriter jsonWriter = new GsonJsonWriter(gson);
    static final MP3Processor mp3Processor = new MP3Processor(new JAudioTaggerMP3MetadataReader(), new ProcessMetadataToRequestString());

    @Override
    public void start(Stage stage) throws Exception {
        String dbPath = UserConfig.getDatabasePath();
        if (dbPath == null) {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Choose database folder");
            File dir = chooser.showDialog(stage);

            if (dir != null) {
                dbPath = dir.getAbsolutePath();
                UserConfig.setDatabasePath(dbPath);
            } else {
                Platform.exit();
                return;
            }
        }

        MainDatabase mainDatabase = new MainDatabase(new MusicSQLiteDatabase(dbPath, DBName));
        System.out.println("database created");
        Path tempFolderForNewFiles = new TempFolder(Paths.get(dbPath)).existsOrCreate();
        System.out.println("temp ready");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main.fxml"));
        Scene scene = new Scene(loader.load(), 900, 600);

        MainController mainController = loader.getController();
        mainController.init(mainDatabase, tempFolderForNewFiles, mp3Processor);
        System.out.println("main init ready");

        stage.setScene(scene);
        stage.setTitle("Demo JavaFX + SQLite (FXML)");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }


    private static InputStreamReader getInputStreamReader(String path) {
        return new InputStreamReader(
                Objects.requireNonNull(
                        Main.class.getResourceAsStream(path)
                ),
                StandardCharsets.UTF_8);
    }
}
