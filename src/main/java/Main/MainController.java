package Main;

import app.control.TokenManager;
import app.model.utilities.database.Database;
import com.google.gson.Gson;
import dependencies.spotify.model.auth.ClientCredentials;
import dependencies.spotify.model.auth.TokenRequest;
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
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainController {

    // --- statics ---
    static final Gson gson = new Gson();
    static final ClientCredentials credentials =
            gson.fromJson(getInputStreamReader("/client-credentials.json"), ClientCredentials.class);
    static final TokenManager tokenManager =
            new dependencies.spotify.control.TokenManager(HttpClient.newHttpClient(), new TokenRequest(credentials));
    static final Database database = database();


    private static Database database(){
        return new Database() {
            String folder = "C:\\Users\\santi\\Desktop\\musica";
            String name = "music.db";
            Connection connection;

            private void execute(String sql){
                try (Statement st = connection.createStatement()) {
                    st.execute(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void initialize() {
                for (Tables t: Tables.values()){
                    System.out.println("executing: " + t.definition());
                    execute(t.definition());
                }
            }

            @Override
            public String url() {
                return folder + "\\" + name;
            }

            @Override
            public String folder() {
                return folder;
            }

            @Override
            public String name() {
                return name;
            }

            @Override
            public void createTable(String definition) {
                execute(definition);
            }

            @Override
            public void deleteTable(String name) {
                execute("DROP TABLE IF EXISTS " + name + ";");
            }

            @Override
            public List<String> tables() {
                List<String> tables = new ArrayList<>();
                String sql = "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;";

                try (PreparedStatement stmt = connection.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        tables.add(rs.getString("name"));
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                return tables;
            }


            @Override
            public void open() {
                try {
                    connection = DriverManager.getConnection(this.url());
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

    private static InputStreamReader getInputStreamReader(String path) {
        return new InputStreamReader(
                Objects.requireNonNull(
                        Main.class.getResourceAsStream(path)
                ),
                StandardCharsets.UTF_8);
    }

    // --- SQLite ---
    static class Data {
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
        System.out.println("database initializing");
        database.initialize();
        System.out.println("database initialized");

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
        tableData.setAll(Data.listAll());
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
            Data.insertOrIgnore(path, name, type);
        }
    }
}

