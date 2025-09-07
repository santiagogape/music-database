package dependencies.view.JavaFX.controllers.files;

import com.google.gson.Gson;
import dependencies.spotify.model.items.SpotifyTrackSearchResponse;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonEditorController {
    public TextArea textArea;
    private Path currentFile;
    private Gson gson;
    private boolean filling;
    private boolean saved;

    @FXML
    private void initialize(){
        this.filling = true;
        this.saved = true;
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!filling) {
                System.out.println("Contenido modificado por el usuario!");
            }
        });
    }

    public void init(Gson gson){
        this.gson = gson;
    }

    public void onOpenFile(Path path) {
        currentFile = path;
        try {
            filling = true;
            String content = Files.readString(currentFile);
            filling = false;
            saved = false;
            SpotifyTrackSearchResponse spotifyTrackSearchResponse = this.gson.fromJson(content, SpotifyTrackSearchResponse.class);

            textArea.setText(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onSaveFile(ActionEvent actionEvent) {
        if (currentFile != null) {
            try {
                Files.writeString(currentFile, textArea.getText());
                saved = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isSaved() {return saved;}
}
