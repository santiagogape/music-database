package Main;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    // --- Modelo simple ---
        public record ObjectRecord(int id, String path, String name, String type) {
    }

    // --- SQLite ---
    static class Database {
        private static final String DB_URL = "jdbc:sqlite:music.db";

        static Connection get() throws SQLException {
            Connection c = DriverManager.getConnection(DB_URL);
            try (Statement st = c.createStatement()) {
                st.execute("PRAGMA foreign_keys = ON;");
            }
            return c;
        }

        static void init() {
            String ddl = """
                CREATE TABLE IF NOT EXISTS OBJECTS (
                  ID   INTEGER PRIMARY KEY AUTOINCREMENT,
                  PATH TEXT    NOT NULL UNIQUE,
                  NAME TEXT    NOT NULL,
                  TYPE TEXT    NOT NULL
                );
                """;
            try (Connection cn = get();
                 Statement st = cn.createStatement()) {
                st.execute(ddl);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        static void insertOrIgnore(String path, String name, String type) {
            String sql = "INSERT OR IGNORE INTO OBJECTS(PATH, NAME, TYPE) VALUES (?,?,?)";
            try (Connection cn = get();
                 PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, path);
                ps.setString(2, name);
                ps.setString(3, type);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        static List<ObjectRecord> listAll() {
            List<ObjectRecord> out = new ArrayList<>();
            String sql = "SELECT ID, PATH, NAME, TYPE FROM OBJECTS ORDER BY ID DESC";
            try (Connection cn = get();
                 PreparedStatement ps = cn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(new ObjectRecord(
                            rs.getInt("ID"),
                            rs.getString("PATH"),
                            rs.getString("NAME"),
                            rs.getString("TYPE")
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return out;
        }
    }

    // --- Referencias desde FXML ---
    @FXML private StackPane dropZone;
    @FXML private TableView<ObjectRecord> tableView;
    @FXML private TableColumn<ObjectRecord, Number> colId;
    @FXML private TableColumn<ObjectRecord, String> colName;
    @FXML private TableColumn<ObjectRecord, String> colPath;
    @FXML private TableColumn<ObjectRecord, String> colType;

    private final ObservableList<ObjectRecord> tableData = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        Database.init();

        tableView.setItems(tableData);
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().id()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().name()));
        colPath.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().path()));
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().type()));

        // Drag & Drop
        dropZone.setOnDragOver(this::onDragOver);
        dropZone.setOnDragDropped(this::onDragDropped);

        refreshTable();
    }

    @FXML
    private void onRefresh() {
        refreshTable();
    }

    private void onDragOver(DragEvent e) {
        Dragboard db = e.getDragboard();
        if (db.hasFiles()) {
            e.acceptTransferModes(TransferMode.COPY);
        }
        e.consume();
    }

    private void onDragDropped(DragEvent e) {
        Dragboard db = e.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            List<File> files = db.getFiles();
            new Thread(() -> {
                for (File f : files) {
                    importFileOrDir(f);
                }
                Platform.runLater(this::refreshTable);
            }).start();
            success = true;
        }
        e.setDropCompleted(success);
        e.consume();
    }

    private void refreshTable() {
        tableData.setAll(Database.listAll());
    }

    private void importFileOrDir(File f) {
        if (f.isDirectory()) {
            File[] children = f.listFiles();
            if (children != null) {
                for (File child : children) {
                    importFileOrDir(child);
                }
            }
        } else {
            String path = f.getAbsolutePath();
            String name = f.getName();
            String type = "file";
            Database.insertOrIgnore(path, name, type);
        }
    }
}

