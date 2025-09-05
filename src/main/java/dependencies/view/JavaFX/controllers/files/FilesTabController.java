package dependencies.view.JavaFX.controllers.files;

import app.control.files.MP3Processor;
import app.control.files.listener.FileSongListener;
import app.control.files.listener.FileSongProcessor;
import app.model.items.FileSong;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FilesTabController {


    @FXML public ListView<String> listFiles;
    @FXML public ListView<String> listRequests;
    @FXML public Button requestButton;
    @FXML public VBox responses;

    private MP3Processor processor;
    private FileSongProcessor fileSongProcessor;
    private Path tempFolder;
    private FileSongListener fileSonListener;

    @FXML
    private void initialize(){
        System.out.println("files in");
    }

    public void init(MP3Processor processor, FileSongProcessor fileSongProcessor, Path temp){
        this.processor = processor;
        this.fileSongProcessor = fileSongProcessor;
        this.tempFolder = temp;
        this.fileSonListener = createFileSongListenerForRequestingToSpotify();
        System.out.println("files init");
    }

    private FileSongListener createFileSongListenerForRequestingToSpotify() {
        return new FileSongListener() {
            private final List<FileSong> list = new ArrayList<>();
            @Override
            public List<FileSong> consume() {
                List<FileSong> copy = List.copyOf(list);
                list.clear();
                return copy;
            }

            @Override
            public void receive(List<FileSong> fileSongs) {
                list.addAll(fileSongs);
            }
        };
    }

    public void onReadTemp(ActionEvent ignored) {
        try (Stream<Path> list = Files.list(this.tempFolder)
                .filter(Files::isRegularFile)
                .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".mp3"))
                ) {
            list.forEach(e -> listFiles.getItems().add(e.getFileName().toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        updateFileListForProcess();
    }

    private void updateFileListForProcess() {
        this.fileSonListener.receive(fileSongProcessor.process(this.processor.process(tempFolder)));
        this.requestButton.setVisible(true);
        this.responses.setVisible(true);
    }

    public void onStartRequesting(ActionEvent ignored) {
        List<FileSong> consume = this.fileSonListener.consume();
        ObservableList<String> items = this.listRequests.getItems();
        consume.stream().map(FileSong::request).forEach(items::add);
        this.requestButton.setVisible(false);
    }
}
