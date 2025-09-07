package Main;

import Main.Database.MainDatabase;
import app.control.TokenManager;
import app.control.files.actors.MP3Processor;
import app.control.files.listener.FileSongProcessor;
import app.model.items.FileSong;

import com.google.gson.Gson;
import dependencies.view.JavaFX.controllers.files.FilesTabController;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;

import java.nio.file.Path;
import java.util.List;

public class MainController {

    private MainDatabase mainDatabase;
    private Path tempFolder;
    private MP3Processor mp3Processor;
    private TokenManager tokenManager;

    @FXML HBox filesTab;
    @FXML FilesTabController filesTabController;

    private FileSongProcessor fileSongListener;

    public void init(MainDatabase mainDatabase, Path tempFolder, MP3Processor mp3Processor, TokenManager tokenManager, Gson gson) {
        this.mainDatabase = mainDatabase;
        this.tempFolder = tempFolder;
        this.mp3Processor = mp3Processor;
        this.tokenManager = tokenManager;
        System.out.println("main init");
        filesTabController.init(this.mp3Processor,fileSongListener,this.tempFolder, gson);
    }

    @FXML
    private void initialize() {
        System.out.println("main in");
        this.fileSongListener = createFileSongListener();
    }


    private FileSongProcessor createFileSongListener() {
        return this::addFileSongs;
    }

    private List<FileSong> addFileSongs(List<FileSong> fileSongs){
        fileSongs.forEach(f -> System.out.println(f.name()));
        return fileSongs.stream().map(f -> this.mainDatabase.getFiles().getFilesTable().insert(f)).toList();
        // this.mainDatabase.getDatabase().commit();
    }




}

