package Main.Database;

import dependencies.model.SQLite.MusicSQLiteDatabase;

import java.sql.*;


public class MainDatabase {


    private final MusicSQLiteDatabase database;
    private final DatabaseFiles files;
    private final DatabaseItems items;
    private final DatabaseRelations relations;


    public MainDatabase(MusicSQLiteDatabase database) {
        this.database = database;
        database.open(); // @REQUIRED 1
        database.initialize(); // @REQUIRED 2
        Connection connection = database.getConnection();
        this.files = new DatabaseFiles(connection);
        this.items = new DatabaseItems(connection);
        this.relations = new DatabaseRelations(connection);
    }

    public MusicSQLiteDatabase getDatabase() {
        return database;
    }

    public DatabaseFiles getFiles() {
        return files;
    }

    public DatabaseItems getItems() {
        return items;
    }

    public DatabaseRelations getRelations() {
        return relations;
    }
}
