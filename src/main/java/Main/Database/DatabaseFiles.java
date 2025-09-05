package Main.Database;

import dependencies.model.SQLite.tables.files.FilesTable;
import dependencies.model.SQLite.tables.files.ImagesTable;
import dependencies.model.SQLite.tables.files.ResponsesTable;

import java.sql.Connection;

public class DatabaseFiles {


    private final FilesTable filesTable;
    private final ImagesTable imagesTable;
    private final ResponsesTable responsesTable;

    enum table {files, images, responses}

    public DatabaseFiles(Connection connection) {
        filesTable = new FilesTable(connection);
        imagesTable = new ImagesTable(connection);
        responsesTable = new ResponsesTable(connection);
    }

    public FilesTable getFilesTable() {
        return filesTable;
    }

    public ImagesTable getImagesTable() {
        return imagesTable;
    }

    public ResponsesTable getResponsesTable() {
        return responsesTable;
    }
}
