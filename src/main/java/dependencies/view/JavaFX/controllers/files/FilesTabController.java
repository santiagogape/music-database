package dependencies.view.JavaFX.controllers.files;

import app.control.files.actors.MP3PathsReader;
import app.control.files.actors.MP3Processor;
import app.control.files.listener.FileSongListener;
import app.control.files.listener.FileSongProcessor;
import app.model.items.FileSong;
import com.google.gson.Gson;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FilesTabController {


    @FXML public ListView<String> listFiles;
    @FXML public ListView<Item> listRequests;
    @FXML public Button requestButton;
    @FXML public VBox responses;
    @FXML public Button readTempButton;
    @FXML public Button readFolderButton;
    @FXML public VBox jsonEditor;
    @FXML public JsonEditorController jsonEditorController;
    @FXML public VBox files;

    private MP3Processor processor;
    private FileSongProcessor fileSongProcessor;
    private Path tempFolder;
    private FileSongListener fileSonListener;
    private Path baseFolder;
    private Gson gson;

    private List<Button> readButtons;

    @FXML
    private void initialize(){
        System.out.println("files in");
        readButtons = List.of(readTempButton,readFolderButton);

        listRequests.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Item item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.getText());
                    if (item.isStatus()) {
                        setStyle("-fx-background-color: lightgreen;");
                    } else {
                        setStyle("-fx-background-color: salmon;");
                    }

                    item.statusProperty().addListener((obs, oldVal, newVal) -> {
                        if (newVal) {
                            setStyle("-fx-background-color: lightgreen;");
                        } else {
                            setStyle("-fx-background-color: salmon;");
                        }
                    });
                }
            }
        });
    }

    public void init(MP3Processor processor, FileSongProcessor fileSongProcessor, Path temp, Gson gson){
        this.processor = processor;
        this.fileSongProcessor = fileSongProcessor;
        this.tempFolder = temp;
        this.baseFolder = temp.getParent();
        this.gson = gson;
        this.fileSonListener = createFileSongListenerForRequestingToSpotify();
        System.out.println("files init");
        jsonEditorController.init(gson);
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
        this.readFolderButton.setVisible(false);
        addToListFiles(MP3PathsReader.fromFolder(this.tempFolder));
        updateFileListForProcess(this.readFolderButton);
    }

    private void addToListFiles(List<Path> list) {
        list.forEach(e -> listFiles.getItems().add(e.getFileName().toString()));
    }

    private void updateFileListForProcess(Button buttonToHide) {
        this.fileSonListener.receive(fileSongProcessor.process(this.processor.process(tempFolder)));
        this.requestButton.setVisible(true);
        this.responses.setVisible(true);
        buttonToHide.setVisible(false);
    }

    public void onReadFolder(ActionEvent actionEvent) {
        File selectedDirectory = FolderChooserForReadingFiles(actionEvent);
        this.readTempButton.setVisible(false);
        if (selectedDirectory != null) {
            Path chosenPath = selectedDirectory.toPath();
            System.out.println("chosen: " + chosenPath);
            addToListFiles(MP3PathsReader.fromFolder(chosenPath));
            updateFileListForProcess(this.readFolderButton);
        }

    }

    private File FolderChooserForReadingFiles(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        File initialDir = baseFolder.toFile();
        directoryChooser.setInitialDirectory(initialDir);

        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

        return directoryChooser.showDialog(stage);
    }

    public void onStartRequesting(ActionEvent ignored) {
        Strings strings = this.fromJsonResource("/temp/db.original-formated.json", Strings.class);
        ObservableList<Item> items = this.listRequests.getItems();
        //this.fileSonListener.consume().stream().map(FileSong::request).forEach(items::add);
        strings.list.forEach(s -> items.add(new Item(s,false)));
        this.requestButton.setVisible(false);
        this.files.setVisible(false);
        openJsonEditor(this.listRequests);
    }

    private void openJsonEditor(ListView<Item> list) {
        list.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (oldVal == null && newVal != null) {
                int newIndex = list.getItems().indexOf(newVal);
                jsonEditorController.onOpenFile(Path.of(jsonPathFrom(String.valueOf(newIndex))));
            } else if (newVal != null && !Objects.equals(oldVal, newVal)) {
                System.out.println("Seleccionado: " + newVal);
                int oldIndex = list.getItems().indexOf(oldVal);
                int newIndex = list.getItems().indexOf(newVal);
                if (!jsonEditorController.isSaved()) {
                    list.getSelectionModel().select(oldIndex);
                    return;
                }
                oldVal.setStatus(true);
                jsonEditorController.onOpenFile(Path.of(jsonPathFrom(String.valueOf(newIndex))));
            }
        });

        ObservableList<Item> items = list.getItems();
        Runnable checkAllTrue = () -> {
            boolean allTrue = items.stream().allMatch(Item::isStatus);
            if (allTrue) {
                System.out.println("🎉 Todos son true!");
            }
        };

        for (Item it : items) {
            it.statusProperty().addListener((obs, oldVal, newVal) -> checkAllTrue.run());
        }
        
        list.getSelectionModel().selectFirst();
        this.jsonEditor.setVisible(true);

    }


    //mock
    private static InputStreamReader getInputStreamReader(String path) {
        return new InputStreamReader(
                Objects.requireNonNull(
                        FilesTabController.class.getResourceAsStream(path)
                ),
                StandardCharsets.UTF_8);
    }

    private <T> T fromJsonResource(String path, Class<T> tClass){
        return this.gson.fromJson(getInputStreamReader(path), tClass);
    }
    record Strings(List<String> list){}
    private static String jsonPathFrom(String query) {
        return "C:\\Users\\santi\\Desktop\\desarrollo\\music-app\\music-database-v1\\src\\main\\resources\\examples\\api-responses\\"+query.replaceAll("[\\\\/:*?\"<>|]", " ")+" query.json";
    }


    public static class Item {
        private final StringProperty text = new SimpleStringProperty();
        private final BooleanProperty status = new SimpleBooleanProperty(false);

        public Item(String text, boolean status) {
            this.text.set(text);
            this.status.set(status);
        }

        public StringProperty textProperty() { return text; }
        public BooleanProperty statusProperty() { return status; }

        public String getText() { return text.get(); }
        public void setText(String text) { this.text.set(text); }

        public boolean isStatus() { return status.get(); }
        public void setStatus(boolean status) { this.status.set(status); }
    }
}
