package dependencies.view.JavaFX.controllers.database;

import app.view.controllers.RefreshableView;
import app.model.utilities.database.Database;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.util.*;

public class DatabaseTabController {

    private final Map<Database.Tables, Node> tableViews = new HashMap<>();
    private final Map<Database.Tables, String> tablesFxml = getFXMLs();
    private final Map<Database.Tables, RefreshableView> tablesControllers = new HashMap<>();

    private Map<Database.Tables, String> getFXMLs() {
        Map<Database.Tables, String> map = new HashMap<>();
        map.put(Database.Tables.FILES, "\\view\\data\\FilesTableView.fxml");
        map.put(Database.Tables.RESPONSES, "\\view\\data\\ResponsesTableView.fxml");
        return map;
    }


    @FXML public ComboBox<Database.Tables> comboTables; // from Database.Tables
    @FXML public StackPane tableContainer;

    @FXML
    public void initialize(){
        comboTables.getItems().addAll(Database.Tables.FILES, Database.Tables.RESPONSES); //todo: use getTableNames()

        comboTables.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                hideOldShowNewView(oldVal, newVal);
            }
        });

        comboTables.getSelectionModel().selectFirst();
    }

    private void hideOldShowNewView(Database.Tables oldVal, Database.Tables newVal) {
        if (!(tableViews.containsKey(newVal))) loadTableView(newVal, tablesFxml.get(newVal));
        Node temp = tableViews.get(oldVal);
        temp.setVisible(false);
        temp.setManaged(false);
        temp = tableViews.get(newVal);
        temp.setVisible(true);
        temp.setManaged(true);
    }

    private void loadTableView(Database.Tables newVal, String s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(s));
            Node node = loader.load();
            Object controller = loader.getController();
            assert controller instanceof RefreshableView;
            node.setVisible(false);
            node.setManaged(false);
            tableContainer.getChildren().add(node);
            tablesControllers.put(newVal, (RefreshableView) controller);
            tableViews.put(newVal, node );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static List<String> getTableNames() {
        return Arrays.stream(Database.Tables.values()).map(Enum::name).toList();
    }


}
