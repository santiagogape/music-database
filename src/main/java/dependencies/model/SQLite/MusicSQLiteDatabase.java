package dependencies.model.SQLite;

import app.model.utilities.database.Database;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class MusicSQLiteDatabase implements Database {

    private final String folder;
    private final String name;
    private final String url;
    private Connection connection = null;

    public MusicSQLiteDatabase(String folder, String name) {
        this.folder = folder;
        this.name = name;
        this.url = "jdbc:sqlite:" + folder + "\\" + name;
    }

    private void execute(String sql){
        try (Statement st = connection.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static String now(){
        return LocalDateTime.now().format(fmt);
    }
    public static String LocalDateTimeToString(LocalDateTime time){
        return time.format(fmt);
    }
    public static LocalDateTime LocalDateTimeFromString(String time){
         return LocalDateTime.parse(time, fmt);
    }

    @Override
    public void initialize() {
        try {
            if (connection == null || connection.isClosed()) throw new RuntimeException("Database isn´t open yet");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        for (Tables t: Tables.values()){
            System.out.println("executing: " + t.definition());
            execute(t.definition());
        }

    }

    @Override
    public String url() {
        return url;
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
            connection = DriverManager.getConnection(this.url);
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

    public Connection getConnection() {
        return connection;
    }
}
